package com.naeayedea.keith.platform.discord.listener.message;

import com.naeayedea.keith.common.exception.KeithExecutionException;
import com.naeayedea.keith.common.exception.KeithGracefulErrorException;
import com.naeayedea.keith.common.exception.KeithPermissionException;
import com.naeayedea.keith.common.i18n.TranslationProvider;
import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.platform.discord.client.CoreApiClient;
import com.naeayedea.keith.platform.discord.client.CoreUserClient;
import com.naeayedea.keith.platform.discord.event.ActiveSessionRegistry;
import com.naeayedea.keith.platform.discord.server.LocalServerSettingsProvider;
import com.naeayedea.keith.common.model.event.KeithEvent;
import com.naeayedea.keith.common.model.user.BasicKeithUser;
import com.naeayedea.keith.common.model.user.KeithUser;
import com.naeayedea.keith.platform.discord.ratelimiter.CommandRateLimiter;
import com.naeayedea.keith.common.util.KeithConstants;
import com.naeayedea.keith.common.util.MultiMap;
import com.naeayedea.keith.platform.discord.command.lib.TextCommandHandler;
import com.naeayedea.keith.platform.discord.listener.AbstractUserPermittingDiscordEventListener;
import com.naeayedea.keith.platform.discord.utils.Utilities;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class MessageReceivedEventListener extends AbstractUserPermittingDiscordEventListener<MessageReceivedEvent> {

    private final Logger logger = LoggerFactory.getLogger(MessageReceivedEventListener.class);

    @Value("${keith.default-prefix}")
    private String DEFAULT_PREFIX;

    private Map<Locale, MultiMap<String, TextCommandHandler>> localeToAliasMap;

    private final ExecutorService commandService;

    private final CoreUserClient userClient;

    private final LocalServerSettingsProvider serverSettings;

    private final CommandRateLimiter rateLimiter;

    private final List<TextCommandHandler> textCommandHandlers;

    private final TranslationProvider translationProvider;

    private final ActiveSessionRegistry activeSessionRegistry;

    private final CoreApiClient coreApiClient;

    public MessageReceivedEventListener(
        @Qualifier("commandService") ExecutorService commandService,
        CoreUserClient userClient,
        LocalServerSettingsProvider serverSettings,
        CommandRateLimiter rateLimiter,
        @Qualifier("userTextCommandHandlers") List<TextCommandHandler> textCommandHandlers,
        TranslationProvider translationProvider,
        ActiveSessionRegistry activeSessionRegistry,
        CoreApiClient coreApiClient
    ) {
        this.userClient = userClient;
        this.serverSettings = serverSettings;
        this.commandService = commandService;
        this.rateLimiter = rateLimiter;

        this.textCommandHandlers = textCommandHandlers;

        this.translationProvider = translationProvider;
        this.activeSessionRegistry = activeSessionRegistry;
        this.coreApiClient = coreApiClient;
    }

    @PostConstruct
    private void initialiseCommands() {
        logger.info("Initializing base command map.");

        localeToAliasMap = new HashMap<>();

        Utilities.populateCommandMap(localeToAliasMap, textCommandHandlers, List.of(), translationProvider);

        System.out.println(localeToAliasMap);

        logger.info("Loaded {} base message command aliases", localeToAliasMap.get(KeithConstants.DEFAULT_LOCALE).size());
    }

    private String getPrefix(MessageReceivedEvent event) {
        if (event.getChannel() instanceof PrivateChannel) {
            return DEFAULT_PREFIX;
        } else {
            return serverSettings.getPrefix(event.getGuild().getId());
        }
    }

    private boolean findPrefix(String message, String prefix) {
        //ensure that message content greater than prefix length then check if prefix is there
        return message.length() > prefix.length() && message.toLowerCase().startsWith(prefix);
    }

    private TextCommandHandler findCommand(List<String> list, Locale locale) {
        String commandString = list.removeFirst().toLowerCase();

        System.out.println(locale.getLanguage());
        System.out.println("Available locals: "+ localeToAliasMap.keySet());

        //if locale in map, return the translations
        if (localeToAliasMap.containsKey(locale)) {
            return localeToAliasMap.get(locale).get(commandString);
        }

        //otherwise use default
        return localeToAliasMap.get(KeithConstants.DEFAULT_LOCALE).get(commandString);
    }

    @EventListener
    @Async
    @Override
    public void onEvent(KeithEvent<MessageReceivedEvent> eventSource) {
        super.onEvent(eventSource);
    }

    @Override
    public boolean serverPermitted(MessageReceivedEvent event) {
        if (event.getChannel() instanceof PrivateChannel) {
            return true;
        }

        return !serverSettings.isBanned(event.getGuild().getId());
    }

    @Override
    public boolean eventIsCompatible(MessageReceivedEvent event) {
        if (!findPrefix(event.getMessage().getContentRaw(), getPrefix(event))) {
            //no prefix - only still interesting if this channel has an active channel-scoped
            //session (a game, a chat relay). Unlike prefixed commands, these bypass rate limiting
            //entirely, same as the old monolith did - you don't want to rate-limit someone
            //spamming guesses in a number game or having an ordinary chat-relay conversation.
            if (!activeSessionRegistry.isActive(event.getChannel().getId())) {
                return false;
            }

            return !userClient.getOrCreateUser(event.getAuthor().getId()).isBanned();
        }

        BasicKeithUser keithUser = userClient.getOrCreateUser(event.getAuthor().getId());

        //Check user isn't rate limited
        if (rateLimitReached(event, keithUser)) {
            return false;
        }

        //not rate limited, proceed
        logger.trace("User {} passed rate limit check.", keithUser.getId());

        String prefix = getPrefix(event);

        //Need to wrap the stringList in an arrayList as stringList does not support removal of indices
        List<String> tokens = new ArrayList<>(Arrays.asList(event.getMessage().getContentRaw().substring(prefix.length()).trim().split("\\s+")));

        TextCommandHandler command = findCommand(tokens, keithUser.getLocale());

        if (command == null) {
            return false;
        }

        if (permissionsNotMet(event, keithUser, command)) {
            return false;
        }

        MessageChannel channel = event.getChannel();

        if (!(channel instanceof TextChannel)) {
            return true;
        }

        return privateMessageCompatible(event, command, channel, keithUser);
    }


    @Override
    public void onPermitted(MessageReceivedEvent event) throws Exception {
        MessageChannel channel = event.getChannel();

        //automatically join any threads that are created so that bot feels easy to use in threads
        if (channel instanceof ThreadChannel thread && !thread.isJoined()) {
            thread.join().queue();
        }

        if (!findPrefix(event.getMessage().getContentRaw(), getPrefix(event))) {
            evaluateChannelSession(event);

            return;
        }

        BasicKeithUser keithUser = userClient.getOrCreateUser(event.getAuthor().getId());

        String prefix = getPrefix(event);

        //Need to wrap the stringList in an arrayList as stringList does not support removal of indices
        List<String> tokens = new ArrayList<>(Arrays.asList(event.getMessage().getContentRaw().substring(prefix.length()).trim().split("\\s+")));

        TextCommandHandler command = findCommand(tokens, keithUser.getLocale());

        logger.trace("Found command: {}", command.getInternalName());

        //all checks passed, execute command
        Runnable execution = () -> {
            try {
                command.run(event, tokens);
            } catch (KeithPermissionException e) {
                event.getMessage()
                    .reply("You do not have access to this command.")
                    .queue();
            } catch (KeithGracefulErrorException e) {
                event.getMessage()
                    .reply(e.getMessage())
                    .queue();
            } catch (KeithExecutionException e) {
                event.getMessage()
                    .reply("Something went wrong :(")
                    .queue();
            }

            //TODO: core doesn't expose a way to increment a user's command count over HTTP yet
            //(KeithUserManager.incrementCommandCount is in-process only) - re-add once it does.
        };

        //increment the rate limit before proceeding, if the command fails we don't want the user to be able to spam
        rateLimiter.incrementOrInsertRecord(keithUser.getId(), command.getCost());

        Future<?> commandFuture =  commandService.submit(execution);

        try {
            commandFuture.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException e) {
            //interrupt the task
            commandFuture.cancel(true);

            //rethrow so we can handle higher up
            throw new TimeoutException();
        }
    }

    /**
     * Forwards a plain (non-prefixed) message in a channel {@link ActiveSessionRegistry} believes
     * has an active session to core for evaluation - a guess, a chat-relay message, anything a
     * channel-scoped session cares about. Most channels never reach this since the registry check
     * in {@link #eventIsCompatible(MessageReceivedEvent)} already filtered them out.
     */
    private void evaluateChannelSession(MessageReceivedEvent event) {
        Member member = event.getMember();
        String authorName = member != null ? member.getEffectiveName() : event.getAuthor().getName();

        Map<String, String> params = new LinkedHashMap<>();

        params.put("channelId", event.getChannel().getId());
        params.put("authorName", authorName);
        params.put("content", event.getMessage().getContentRaw());

        TextResponse response = coreApiClient.getAsUser(
            "/api/v1/command/channel/evaluate",
            event.getAuthor().getId(),
            params,
            TextResponse.class
        );

        if (response == null) {
            return;
        }

        if (!response.getText().isBlank()) {
            event.getMessage().reply(response.getText()).queue();
        }

        for (String reaction : response.getReactions()) {
            event.getMessage().addReaction(Emoji.fromUnicode(reaction)).queue();
        }
    }

    private boolean privateMessageCompatible(MessageReceivedEvent event, TextCommandHandler command, MessageChannel channel, BasicKeithUser keithUser) {
        if (!command.isPrivateMessageCompatible()) {
            //consider this a command, albeit a small penalty
            rateLimiter.incrementOrInsertRecord(keithUser.getId(), 1);

            event.getMessage()
                .reply(command.getInternalName() + " cannot be used in private message!")
                .queue();

            return false;
        }

        return true;
    }

    private boolean permissionsNotMet(MessageReceivedEvent event, BasicKeithUser keithUser, TextCommandHandler command) {
        if (!keithUser.hasPermission(command.getAccessLevel())) {
            logger.trace("User {} does not have permission to use command {}", keithUser.getId(), command.getInternalName());

            //consider this a command, albeit a small penalty
            rateLimiter.incrementOrInsertRecord(keithUser.getId(), 1);

            event.getMessage()
                .reply("You do not have access to this command")
                .queue();

            return true;
        }

        return false;
    }

    private boolean rateLimitReached(MessageReceivedEvent event, BasicKeithUser keithUser) {
        if (!rateLimiter.userPermitted(keithUser.getId())) {
            logger.trace("User {} has been rate limited.", keithUser.getId());

            //consider this a command, albeit a small penalty
            rateLimiter.incrementOrInsertRecord(keithUser.getId(), 1);

            //if user significantly above rate limit max then we just ignore them.
            if (rateLimiter.getCurrentValue(keithUser.getId()) < rateLimiter.DEFAULT_RATE_LIMIT * 2) {
                event.getMessage()
                    .reply("Too many commands in a short time.. please wait 30 seconds")
                    .queue();
            }

            return true;
        }

        return false;
    }

    @Override
    public void onRejected(MessageReceivedEvent event) {

    }

    @Override
    public void onError(MessageReceivedEvent event, Throwable e) {
        if (e == null) {
            logger.error("Received null throwable from onError call. event: {}", event);

            return;
        }

        switch (e) {
            case PermissionException permissionException -> event.getMessage()
                .reply("I need more permissions to do that!")
                .queue();
            case IllegalArgumentException illegalArgumentException -> event.getMessage()
                .reply("Invalid Arguments")
                .queue();
            case TimeoutException timeoutException -> event.getMessage()
                .reply("Execution of command took too long.")
                .queue();
            case KeithPermissionException keithPermissionException -> event.getMessage()
                .reply("You do not have access to this command.")
                .queue();
            case KeithGracefulErrorException keithGracefulErrorException -> event.getMessage()
                .reply(e.getMessage())
                .queue();
            case KeithExecutionException keithExecutionException -> event.getMessage()
                .reply("Something went wrong :(")
                .queue();
            default -> logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected KeithUser getUser(MessageReceivedEvent event) {
        return userClient.getOrCreateUser(event.getAuthor().getId());
    }

    @Override
    protected boolean isBot(MessageReceivedEvent event) {
        User author = event.getAuthor();

        return author.isBot() || author.isSystem();
    }

}

