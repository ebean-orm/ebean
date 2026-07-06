package io.ebean.redisson.dto;

import java.util.Objects;

public class L2QueryInvalidMessage implements L2Message {
    private String serverId;
    private String key;

    @Override
    public String toString() {
        return "L2QueryInvalidMessage{" +
               "serverId='" + serverId + '\'' +
               ", key='" + key + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof L2QueryInvalidMessage)) return false;
        L2QueryInvalidMessage that = (L2QueryInvalidMessage) o;
        return Objects.equals(getServerId(), that.getServerId()) && Objects.equals(getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerId(), getKey());
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
