package io.ebean.redisson.dto;

import java.util.Objects;
import java.util.Set;

public class L2TableModMessage implements L2Message {
    private String serverId;
    private Set<String> tables;

    @Override
    public String toString() {
        return "L2TableModMessage{" +
               "serverId='" + serverId + '\'' +
               ", tables=" + tables +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof L2TableModMessage)) return false;
        L2TableModMessage that = (L2TableModMessage) o;
        return Objects.equals(getServerId(), that.getServerId()) && Objects.equals(getTables(), that.getTables());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerId(), getTables());
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Set<String> getTables() {
        return tables;
    }

    public void setTables(Set<String> tables) {
        this.tables = tables;
    }
}
