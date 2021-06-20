package keith;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

public class Bot {

        String token;
        DataSource database;

        public Bot (String token, DataSource database) {
            this.token = token;
            this.database = database;
        }

        public void build(){
            JDABuilder builder = JDABuilder.create(token, GatewayIntent.GUILD_MEMBERS,   GatewayIntent.GUILD_BANS,
                    GatewayIntent.GUILD_EMOJIS,   GatewayIntent.GUILD_VOICE_STATES,      GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.DIRECT_MESSAGE_REACTIONS);
            builder.setMemberCachePolicy(MemberCachePolicy.ALL);
            builder.setChunkingFilter(ChunkingFilter.ALL);
            builder.setLargeThreshold(50);
            try{
                JDA jda = builder.build();
                jda.awaitReady();
                jda.addEventListener(new EventHandler(database, jda));
            } catch (LoginException e){
                e.printStackTrace();
            } catch (InterruptedException e){
                //do nothing
            }
        }
}
