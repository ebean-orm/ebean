package io.ebean.redisson.encode;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SerializableCodec} value encoder/decoder.
 * Mirrors ebean-redis {@code EncodeSerializableTest}.
 */
class SerializableCodecTest {

  private final SerializableCodec codec = new SerializableCodec();
  private final Encoder encoder = codec.getValueEncoder();
  private final Decoder<Object> decoder = codec.getValueDecoder();

  @Test
  void roundTrip_string() throws Exception {
    ByteBuf buf = encoder.encode("HelloWorld");
    Object result = decoder.decode(buf, null);
    assertThat(result).isEqualTo("HelloWorld");
  }

  @Test
  void roundTrip_long() throws Exception {
    ByteBuf buf = encoder.encode(42L);
    Object result = decoder.decode(buf, null);
    assertThat(result).isEqualTo(42L);
  }

  @Test
  void roundTrip_list() throws Exception {
    List<String> original = List.of("a", "b", "c");
    ByteBuf buf = encoder.encode(original);
    Object result = decoder.decode(buf, null);
    assertThat(result).isEqualTo(original);
  }

  @Test
  void roundTrip_null() throws Exception {
    ByteBuf buf = encoder.encode(null);
    Object result = decoder.decode(buf, null);
    assertThat(result).isNull();
  }
}
