package succ;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;

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
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.setLargeThreshold(50);
        try {
            JDA jda = builder.build();

            try{
                //Sleep thread so bot waits a second before coming online - e.g. let bot log in
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //Dont matter
            }
            jda.addEventListener(new EventHandler(jda, url));
        } catch (LoginException e) {
            e.printStackTrace();
        }


    }


}
