package keith;


import com.github.ygimenez.exception.InvalidHandlerException;
import com.github.ygimenez.model.PaginatorBuilder;
import keith.util.logs.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

public class Bot {

        String token;
        String restartMessage;
        String restartChannel;
        DataSource database;

        public Bot (String token, DataSource database, String restartMessage, String restartChannel) {
            this.token = token;
            this.database = database;
            this.restartMessage = restartMessage;
            this.restartChannel = restartChannel;
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
                Thread.sleep(500);

                //configure pagination library
                PaginatorBuilder.createPaginator()
                        .setHandler(jda)
                        .shouldRemoveOnReact(false)
                        .shouldEventLock(true)
                        .activate();

                jda.addEventListener(new EventHandler(database, jda, restartMessage, restartChannel));
            } catch (LoginException e){
                e.printStackTrace();
            } catch (InterruptedException e){
                //do nothing
            } catch (InvalidHandlerException e) {
                Logger.printWarning(e.getMessage());
            }
        }
}
