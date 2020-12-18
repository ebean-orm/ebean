package io.ebean.redis.encode;

import io.ebean.cache.TenantAwareKey;

import java.nio.charset.StandardCharsets;

public class EncodePrefixKey implements Encode {

  private final String prefix;

  public EncodePrefixKey(String cacheKey) {
    this.prefix = cacheKey + ":";
  }

  @Override
  public byte[] encode(Object value) {
    try {
      if (!(value instanceof String) && !(value instanceof TenantAwareKey.CacheKey)) {
        throw new IllegalStateException("Expecting String keys but got type:" + value.getClass());
      }

      String key = prefix + value.toString();
      return key.getBytes(StandardCharsets.UTF_8);

    } catch (Exception e) {
      throw new RuntimeException("Failed to decode cache data", e);
    }
  }

  @Override
  public Object decode(byte[] data) {
    try {
      String key = new String(data, StandardCharsets.UTF_8);
      int pos = key.indexOf(':');
      return key.substring(pos);

    } catch (Exception e) {
      throw new RuntimeException("Failed to decode cache data", e);
    }
  }
}
