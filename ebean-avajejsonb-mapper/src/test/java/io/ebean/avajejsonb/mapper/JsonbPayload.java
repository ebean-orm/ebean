package io.ebean.avajejsonb.mapper;

import io.avaje.jsonb.Json;

import java.util.Objects;

@Json
public class JsonbPayload {

  public String name;
  public int count;

  public JsonbPayload() {
  }

  JsonbPayload(String name, int count) {
    this.name = name;
    this.count = count;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof JsonbPayload)) {
      return false;
    }
    JsonbPayload other = (JsonbPayload) object;
    return count == other.count && Objects.equals(name, other.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, count);
  }
}
