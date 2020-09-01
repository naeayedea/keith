import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

public class Bot {

    public Bot(String token, String url) {
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.setActivity(Activity.listening("motherfuckers scream"));
        builder.setLargeThreshold(50);
        try {
            JDA jda = builder.build();
            jda.addEventListener(new MessageHandler(jda, url));
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }
}
