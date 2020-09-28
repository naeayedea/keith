package succ;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;

/**
 * Initialises bot and logs into discord.
 */
public class Bot {

    /**
     * Creates a new bot object, should only ever be created once during runtime
     * @Param   token   your bot token goes here, apply in main class
     * @Param   url     your database url
     */
    public Bot(String token, String url){
        //Create jda builder object
        JDABuilder builder = JDABuilder.create(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_BANS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS);
        builder.setActivity(Activity.listening("?help for commands"));  //Default discord status
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setLargeThreshold(50);
        try {
            JDA jda = builder.build();
            jda.addEventListener(new MessageHandler(jda, url));
        } catch (LoginException e) {
            e.printStackTrace();
        }


    }


}
