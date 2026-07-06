package io.ebean.redisson.dto;

import java.util.Arrays;
import java.util.Objects;

public class NearCacheInvalidateKeysMessage implements NearMessage {
    private String serverId;
    private String cacheKey;
    private byte[] keys;

    @Override
    public String toString() {
        return "NearCacheInvalidateKeysMessage{" +
               "serverId='" + serverId + '\'' +
               ", cacheKey='" + cacheKey + '\'' +
               ", keys=" + Arrays.toString(keys) +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NearCacheInvalidateKeysMessage)) return false;
        NearCacheInvalidateKeysMessage that = (NearCacheInvalidateKeysMessage) o;
        return Objects.equals(getServerId(), that.getServerId()) && Objects.equals(getCacheKey(), that.getCacheKey()) && Objects.deepEquals(getKeys(), that.getKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerId(), getCacheKey(), Arrays.hashCode(getKeys()));
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public byte[] getKeys() {
        return keys;
    }

    public void setKeys(byte[] keys) {
        this.keys = keys;
    }
}
