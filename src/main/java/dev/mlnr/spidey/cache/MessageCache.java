package dev.mlnr.spidey.cache;

import dev.mlnr.spidey.objects.messages.MessageData;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static dev.mlnr.spidey.utils.Utils.createDefaultExpiringMap;

public class MessageCache
{
    private static final Map<Long, MessageData> MESSAGE_DATA_CACHE = ExpiringMap.builder()
            .maxSize(10000)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(10, TimeUnit.MINUTES)
            .build();
    private static final Map<Long, Long> LAST_MESSAGE_DELETED_CACHE = createDefaultExpiringMap();

    private static final Map<Long, Long> LAST_MESSAGE_EDITED_CACHE = createDefaultExpiringMap();
    private static final Map<Long, MessageData> LAST_MESSAGE_EDITED_DATA_CACHE = createDefaultExpiringMap();

    private MessageCache() {}

    public static MessageData getLastDeletedMessage(long channelId)
    {
        var latest = LAST_MESSAGE_DELETED_CACHE.get(channelId);
        return latest == null ? null : MESSAGE_DATA_CACHE.get(latest);
    }

    public static void setLastDeletedMessage(long channelId, long messageId)
    {
        LAST_MESSAGE_DELETED_CACHE.put(channelId, messageId);
    }

    public static void cacheMessage(long messageId, MessageData message)
    {
        var data = MESSAGE_DATA_CACHE.get(messageId);
        if (data != null)
            LAST_MESSAGE_EDITED_DATA_CACHE.put(messageId, data);
        MESSAGE_DATA_CACHE.put(messageId, message);
    }

    public static boolean isCached(long messageId)
    {
        return MESSAGE_DATA_CACHE.containsKey(messageId);
    }

    // MESSAGE EDITING CACHING

    public static void setLastEditedMessage(long channelId, long messageId)
    {
        LAST_MESSAGE_EDITED_CACHE.put(channelId, messageId);
    }

    public static MessageData getLastEditedMessage(long channelId)
    {
        var latest = LAST_MESSAGE_EDITED_CACHE.get(channelId);
        return latest == null ? null : LAST_MESSAGE_EDITED_DATA_CACHE.get(latest);
    }

    // other

    public static void pruneCache(long guildId)
    {
        var it = MESSAGE_DATA_CACHE.entrySet().iterator(); // using iterator here instead of a for loop should prevent getting CMEs
        while (it.hasNext())
        {
            var next = it.next();
            var dataGuildId = next.getValue().getGuildId();
            if (dataGuildId != guildId)
                continue;
            var messageId = next.getKey();
            it.remove();
            LAST_MESSAGE_DELETED_CACHE.entrySet().removeIf(entry1 -> entry1.getValue() == messageId);
            LAST_MESSAGE_EDITED_CACHE.entrySet().removeIf(entry1 -> entry1.getValue() == messageId);
            LAST_MESSAGE_EDITED_DATA_CACHE.entrySet().removeIf(entry1 -> entry1.getValue().getGuildId() == guildId);
        }
    }
}