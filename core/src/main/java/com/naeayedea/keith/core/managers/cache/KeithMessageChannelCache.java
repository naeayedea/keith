package com.naeayedea.keith.core.managers.cache;

import com.naeayedea.keith.common.model.channel.KeithChannel;
import com.naeayedea.keith.common.model.channel.KeithMessageChannel;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class KeithMessageChannelCache {

    public static final String CACHE_NAME = "message_channels";

    @Nullable
    @Cacheable(cacheNames = CACHE_NAME, key = "#channelId + #platform")
    public KeithMessageChannel getChannel(@NonNull String channelId, String platform) {
        return null;
    }

    @NonNull
    @CachePut(cacheNames = CACHE_NAME, key = "#channel.id + #channel.platform")
    public KeithMessageChannel putChannel(@NonNull KeithMessageChannel channel) {
        return channel;
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#channel.id + #channel.platform")
    public void removeChannel(@NonNull KeithChannel channel) {
        //any side effects here
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#channelId + #platform")
    public void removeChannel(String channelId, String platform) {
        //any side effects here
    }
}
