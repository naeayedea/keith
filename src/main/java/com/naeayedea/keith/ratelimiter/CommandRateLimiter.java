package com.naeayedea.keith.ratelimiter;

import java.util.HashMap;
import java.util.Map;

public class CommandRateLimiter {

    private final int rateLimitMax;

    private final Map<String, Integer> rateLimitRecord;

    public CommandRateLimiter(int rateLimitMax) {
        this.rateLimitMax = rateLimitMax;
        rateLimitRecord = new HashMap<>();
    }

    public int getCurrentValue(String id) {
        Integer value = rateLimitRecord.get(id);

        return value != null ? value : 0;
    }

    public boolean userPermitted(String id) {
        return getCurrentValue(id) < rateLimitMax;
    }

    public void incrementOrInsertRecord(String id, int incrementAmount) {
        rateLimitRecord.merge(id, incrementAmount, Integer::sum);
    }

    public void clearRecord(String id) {
        rateLimitRecord.remove(id);
    }

    public void clearEntries() {
        rateLimitRecord.clear();
    }
}
