package dev.mlnr.spidey.cache;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ResponseCache
{
    private static final Map<Long, Long> RESPONSE_CACHE = ExpiringMap.builder()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(15, TimeUnit.MINUTES)
            .build();

    private ResponseCache() {}

    public static Long getResponseMessageId(long invokeMessageId)
    {
        return RESPONSE_CACHE.get(invokeMessageId);
    }

    public static void setResponseMessageId(long invokeMessageId, long responseMessageId)
    {
        RESPONSE_CACHE.put(invokeMessageId, responseMessageId);
    }

    public static void removeResponseMessageId(long invokeMessageId)
    {
        RESPONSE_CACHE.remove(invokeMessageId);
    }
}