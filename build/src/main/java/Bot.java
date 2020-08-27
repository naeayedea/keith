import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;

import javax.security.auth.login.LoginException;

public class Bot {

    public Bot(String token, String url) {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        JDA jda = null;
        try {
            jda=builder.setToken(token).buildBlocking();
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.setGame(Game.of(Game.GameType.LISTENING, "Your mum"));
        builder.addEventListener(new MessageHandler(jda, url));
        try {
            builder.buildAsync();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }
}
