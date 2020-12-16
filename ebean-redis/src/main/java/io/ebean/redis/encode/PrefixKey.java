package io.ebean.redis.encode;

import java.io.Serializable;

public class PrefixKey implements Serializable {

  private final String prefix;
  private final Object key;

  PrefixKey(String prefix, Object key) {
    this.prefix = prefix;
    this.key = key;
  }

  public String getPrefix() {
    return prefix;
  }

  public Object getKey() {
    return key;
  }
}
