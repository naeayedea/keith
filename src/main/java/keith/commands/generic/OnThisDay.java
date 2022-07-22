package keith.commands.generic;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.InteractPage;
import com.github.ygimenez.model.Page;
import keith.util.Utilities;
import keith.util.logs.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class OnThisDay extends UserCommand {

    private static final String API_URL = "https://byabbe.se/on-this-day/";
    private static final String[] formatList = {"d LLLL", "dd LLLL", "d LLL", "dd LLL", "dd/MM/", "dd MM", "dd-MM", "dd/MM/yyyy", "dd MM yyyy", "dd-MM-yyyy"};


    public OnThisDay() {
        super("otd");
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"Find out what happened this day in history!\"";
    }

    @Override
    public String getLongDescription() {
        return "Gives a selection of events that happened on this day in the past, use \"?otd [day] [month]\" for a specific " +
                "date or just \"?otd\" for today!";
    }

    //OTD utilises an external api, so increase cost of command so not to rate limit ourselves
    @Override
    public int getCost() {
        return 5;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        LocalDate date;
        if (tokens.size() > 0) {
            date = parseDate(Utilities.stringListToString(tokens));
        } else {
            date = LocalDate.now();
        }
        List<Page> pages = new ArrayList<>();
        if (date != null) {
            try {
                //contact api for the information on that day
                URL api = new URL(API_URL+date.getMonthValue()+"/"+date.getDayOfMonth()+"/events.json");
                HttpURLConnection con = (HttpURLConnection) api.openConnection();
                con.setRequestMethod("GET");
                con.connect();
                //parse json
                JSONObject response = new JSONObject(Utilities.readInputStream(con.getInputStream()));
                con.disconnect();
                JSONArray events = response.getJSONArray("events");
                String day = response.getString("date");
                EmbedBuilder eb;
                int numEvents = events.length();
                int[] index;
                if (numEvents <= 6) {
                    index = IntStream.range(0, numEvents).toArray();
                } else if (numEvents >= 100) {
                    index = ThreadLocalRandom.current().ints(0, 99).distinct().limit(6).toArray();
                } else {
                    index = ThreadLocalRandom.current().ints(0, numEvents).distinct().limit(6).toArray();
                }
                int c = 1; int n = index.length;
                for (int i : index) {
                    JSONObject result = (JSONObject) events.get(i);
                    eb = new EmbedBuilder();
                    eb.setTitle("On This Day || "+day+" "+result.getString("year"));
                    eb.addField("Description", "```"+result.getString("description")+"```", false);
                    eb.setColor(Utilities.getColorFromString(result.getString("description")));

                    eb.setFooter("Page "+c+"/"+n);
                    JSONArray wikipedia = result.getJSONArray("wikipedia");
                    StringBuilder links = new StringBuilder();
                    for (int j = 0; j < Math.min(5, wikipedia.length()); j++) {
                        JSONObject link = (JSONObject) wikipedia.get(j);
                            URL firstLink = new URL(link.getString("wikipedia"));
                            HttpURLConnection getImage= (HttpURLConnection) firstLink.openConnection();
                            getImage.setRequestMethod("GET");
                            getImage.connect();
                            String imageURL = Utilities.getImageURL(Utilities.readInputStream(getImage.getInputStream()));
                            if (!imageURL.equals("")) {
                                eb.setImage(imageURL);
                            }
                            getImage.disconnect();
                    }
                    for (Object wikiPage : wikipedia) {
                        JSONObject link = (JSONObject) wikiPage;
                        links.append(link.getString("title")).append("\n").append(link.getString("wikipedia")).append("\n");
                    }
                    eb.addField("Further Reading", links.toString(), false);
                    pages.add(new InteractPage(eb.build()));
                    c++;
                }
                channel.sendMessageEmbeds((MessageEmbed) pages.get(0).getContent()).queue(success -> Pages.paginate(success, pages, true));
            } catch (IOException e) {
                Logger.printWarning(e.getMessage());
                throw new RuntimeException("Could not locate data source, please contact bot owner naeayedea#5861");
            }
        } else {
            channel.sendMessage("Could not locate that date, please enter a day and month such as 01 January").queue();
        }
    }

    public LocalDate parseDate(String target) {
        for (String format : formatList) {
            try{
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern(format)
                        .parseDefaulting(ChronoField.YEAR, 2020)
                        .toFormatter(Locale.ENGLISH);
                return LocalDate.parse(target, formatter);
            }
            catch (DateTimeParseException ignored) {}
        }
        return null;
    }

}
