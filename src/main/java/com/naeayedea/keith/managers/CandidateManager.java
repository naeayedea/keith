package com.naeayedea.keith.managers;

import com.naeayedea.keith.commands.message.AccessLevel;
import com.naeayedea.keith.model.Candidate;
import com.naeayedea.keith.util.Database;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CandidateManager {

    private static CandidateManager instance;

    @Value("${keith.manager.candidate.statements.getCandidate}")
    private String GET_CANDIDATE_STATEMENT;

    @Value("${keith.manager.candidate.statements.createCandidate}")
    private String CREATE_CANDIDATE_STATEMENT;

    @Value("${keith.manager.candidate.statements.setAccessLevel}")
    private String SET_ACCESS_LEVEL_STATEMENT;

    @Value("${keith.manager.candidate.statements.incrementCommandCount}")
    private String INCREMENT_COMMAND_COUNT_STATEMENT;

    private final Map<String, Candidate> userCache;

    private final Database database;

    public CandidateManager(Database database) {
        this.database = database;

        userCache = new HashMap<>();
    }

    public Candidate getCandidate(String discordID) throws SQLException {
        Candidate candidate = userCache.get(discordID);

        if (candidate == null) {
            //user not in cache, attempt to retrieve from database

            candidate = reloadCandidate(discordID);

            userCache.put(discordID, candidate);
        }
        return candidate;
    }

    public Candidate incrementCommandCount(String discordID) throws SQLException {
        if (database.executeUpdate(INCREMENT_COMMAND_COUNT_STATEMENT, discordID)) {
            return reloadCandidate(discordID);
        }

        throw new SQLException();
    }

    public Candidate setAccessLevel(String discordID, AccessLevel accessLevel) throws SQLException {
        Candidate candidate = getCandidate(discordID);

        if (candidate.getAccessLevel() != AccessLevel.OWNER) {
            if (database.executeUpdate(SET_ACCESS_LEVEL_STATEMENT, accessLevel.num, candidate.getId())) {
                return getCandidate(discordID);
            }
        }

        return candidate;
    }

    public Candidate reloadCandidate(String discordID) throws SQLException {
        List<String> results = database.getStringResult(GET_CANDIDATE_STATEMENT, discordID);

        if (results.size() > 1) {
            String[] result = results.get(1).split("\\s+");
            return new Candidate(discordID, AccessLevel.getLevel(result[2]), result[1], Long.parseLong(result[3]));
        } else {
            //user doesn't exist, need to create
            if (!database.executeUpdate(CREATE_CANDIDATE_STATEMENT, discordID)) {
                throw new SQLException("Failed to create candidate " + discordID);
            }

            return new Candidate(discordID, AccessLevel.USER, Instant.now().toString(), 0);
        }
    }

    public void clear() {
        userCache.clear();
    }

}
