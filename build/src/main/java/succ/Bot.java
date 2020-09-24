package succ;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

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
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.setActivity(Activity.listening("?help for commands"));  //Default discord status
        builder.setLargeThreshold(50);
        try {
            JDA jda = builder.build();
            jda.addEventListener(new MessageHandler(jda, url));
        } catch (LoginException e) {
            e.printStackTrace();
        }


    }


}
