package io.ebean.redisson.dto;

import java.util.Arrays;
import java.util.Objects;

public class NearCacheInvalidateKeyMessage implements NearMessage {
    private String serverId;
    private String cacheKey;
    private byte[] key;

    @Override
    public String toString() {
        return "NearCacheInvalidateKeyMessage{" +
               "serverId='" + serverId + '\'' +
               ", cacheKey='" + cacheKey + '\'' +
               ", key=" + Arrays.toString(key) +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NearCacheInvalidateKeyMessage)) return false;
        NearCacheInvalidateKeyMessage that = (NearCacheInvalidateKeyMessage) o;
        return Objects.equals(getServerId(), that.getServerId()) && Objects.equals(getCacheKey(), that.getCacheKey()) && Objects.deepEquals(getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerId(), getCacheKey(), Arrays.hashCode(getKey()));
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

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }
}
