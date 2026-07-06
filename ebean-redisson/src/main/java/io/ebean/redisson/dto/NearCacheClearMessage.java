package io.ebean.redisson.dto;

import java.util.Objects;

public class NearCacheClearMessage implements NearMessage {
    private String serverId;
    private String cacheKey;

    @Override
    public String toString() {
        return "NearCacheClearMessage{" +
               "serverId='" + serverId + '\'' +
               ", cacheKey='" + cacheKey + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NearCacheClearMessage)) return false;
        NearCacheClearMessage that = (NearCacheClearMessage) o;
        return Objects.equals(getServerId(), that.getServerId()) && Objects.equals(getCacheKey(), that.getCacheKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerId(), getCacheKey());
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
}
