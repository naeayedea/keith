package com.naeayedea.keith.managers;

import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.util.Database;

import com.naeayedea.keith.model.Candidate;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CandidateManager {

    private static CandidateManager instance;
    private final Map<String, Candidate> userCache;

    public CandidateManager(){
        userCache = new HashMap<>();
    }

    public static CandidateManager getInstance() {
        if (instance == null) {
            instance = new CandidateManager();
        }
        return instance;
    }

    private PreparedStatement getCandidate() {
        return Database.prepareStatement("SELECT FirstSeen, UserLevel, CommandCount FROM users WHERE DiscordID = ?");
    }

    private PreparedStatement createCandidate() {
        return Database.prepareStatement("INSERT INTO users (DiscordID) VALUES (?) ");
    }

    public Candidate getCandidate(String discordID) {
        Candidate candidate = userCache.get(discordID);
        if(candidate == null) {
            //user not in cache, attempt to retrieve from database
            ArrayList<String> results = Database.getStringResult(getCandidate(), discordID);
            if (results.size() > 1) {
                String[] result = results.get(1).split("\\s+");
                candidate = new Candidate(discordID, AccessLevel.getLevel(result[2]), result[1], Long.parseLong(result[3]));
            } else {
                //user doesn't exist, need to create
                Database.executeUpdate(createCandidate(), discordID);
                candidate =  new Candidate(discordID, AccessLevel.USER , Instant.now().toString(), 0);
            }
            userCache.put(discordID, candidate);
        }
        return candidate;
    }

    public void clear() {
        userCache.clear();
    }

}
