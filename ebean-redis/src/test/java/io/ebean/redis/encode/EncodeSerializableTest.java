package io.ebean.redis.encode;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EncodeSerializableTest {

  @Test
  void encode() {
    EncodeSerializable encodeSerializable = new EncodeSerializable();
    byte[] asBytes = encodeSerializable.encode("HelloWorld");

    Object result = encodeSerializable.decode(asBytes);
    assertThat(result).isEqualTo("HelloWorld");
  }
}
