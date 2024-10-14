package com.naeayedea.keith.commands.text.generic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.InteractPage;
import com.github.ygimenez.model.Page;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
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

@Component
public class OnThisDay extends AbstractUserTextCommand {

    private static final String API_URL = "https://byabbe.se/on-this-day/";

    private static final String[] formatList = {"d LLLL", "dd LLLL", "d LLL", "dd LLL", "dd/MM/", "dd MM", "dd-MM", "dd/MM/yyyy", "dd MM yyyy", "dd-MM-yyyy"};


    private final ObjectMapper mapper;

    private final Logger logger = LoggerFactory.getLogger(OnThisDay.class);

    public OnThisDay(@Value("${keith.commands.onThisDay.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.onThisDay.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);

        this.mapper = new ObjectMapper();
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"Find out what happened this day in history!\"";
    }

    @Override
    public String getDescription() {
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
        if (!tokens.isEmpty()) {
            date = parseDate(Utilities.stringListToString(tokens));
        } else {
            date = LocalDate.now();
        }
        List<Page> pages = new ArrayList<>();
        if (date != null) {
            try {
                //contact api for the information on that day
                URL api = (new URI(API_URL + date.getMonthValue() + "/" + date.getDayOfMonth() + "/events.json")).toURL();
                HttpURLConnection con = (HttpURLConnection) api.openConnection();
                con.setRequestMethod("GET");
                con.connect();
                //parse json
                JsonNode response = mapper.readTree(Utilities.readInputStream(con.getInputStream()));
                con.disconnect();
                List<JsonNode> events = response.findValues("events");
                String day = response.get("date").toString();
                EmbedBuilder eb;
                int numEvents = events.size();
                int[] index;
                if (numEvents <= 6) {
                    index = IntStream.range(0, numEvents).toArray();
                } else if (numEvents >= 100) {
                    index = ThreadLocalRandom.current().ints(0, 99).distinct().limit(6).toArray();
                } else {
                    index = ThreadLocalRandom.current().ints(0, numEvents).distinct().limit(6).toArray();
                }
                int c = 1;
                int n = index.length;
                for (int i : index) {
                    JsonNode result = events.get(i);
                    eb = new EmbedBuilder();
                    eb.setTitle("On This Day || " + day + " " + result.get("year").toString());
                    eb.addField("Description", "```" + result.get("description").toString() + "```", false);
                    eb.setColor(Utilities.getColorFromString(result.get("description").toString()));

                    eb.setFooter("Page " + c + "/" + n);
                    List<JsonNode> wikipedia = result.findValues("wikipedia");
                    StringBuilder links = new StringBuilder();
                    for (int j = 0; j < Math.min(5, wikipedia.size()); j++) {
                        JsonNode link = wikipedia.get(j);
                        URL firstLink = new java.net.URI(link.get("wikipedia").toString()).toURL();
                        HttpURLConnection getImage = (HttpURLConnection) firstLink.openConnection();
                        getImage.setRequestMethod("GET");
                        getImage.connect();
                        String imageURL = Utilities.getImageURL(Utilities.readInputStream(getImage.getInputStream()));
                        if (!imageURL.isEmpty()) {
                            eb.setImage(imageURL);
                        }
                        getImage.disconnect();
                    }
                    for (JsonNode wikiPage : wikipedia) {
                        links.append(wikiPage.get("title").toString()).append("\n").append(wikiPage.get("wikipedia").toString()).append("\n");
                    }
                    eb.addField("Further Reading", links.toString(), false);
                    pages.add(InteractPage.of(eb.build()));
                    c++;
                }
                channel.sendMessageEmbeds((MessageEmbed) pages.getFirst().getContent()).queue(success -> Pages.paginate(success, pages, true));
            } catch (IOException e) {
                logger.error(e.getMessage());
                throw new RuntimeException("Could not locate data source, please contact bot owner naeayedea#5861");
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else {
            channel.sendMessage("Could not locate that date, please enter a day and month such as 01 January").queue();
        }
    }

    public LocalDate parseDate(String target) {
        for (String format : formatList) {
            try {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern(format)
                    .parseDefaulting(ChronoField.YEAR, 2020)
                    .toFormatter(Locale.ENGLISH);
                return LocalDate.parse(target, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

}
