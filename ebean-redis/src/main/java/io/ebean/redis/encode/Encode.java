package io.ebean.redis.encode;

public interface Encode {

  byte[] encode(Object value);

  Object decode(byte[] data);
}
