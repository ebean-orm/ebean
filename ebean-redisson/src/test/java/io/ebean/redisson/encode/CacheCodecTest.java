package io.ebean.redisson.encode;

import io.ebean.cache.TenantAwareKey;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CacheCodec} key encoder/decoder.
 *
 * Regression guard for the decoder bug where "id:tenantId" was decoded to "tenantId"
 * (only the part after the first colon), causing every tenant-aware getAll() lookup to miss.
 */
class CacheCodecTest {

  // Use SerializableCodec as a concrete CacheCodec (only the key codec matters here)
  private final CacheCodec codec = new SerializableCodec();
  private final Encoder keyEncoder = codec.getMapKeyEncoder();
  private final Decoder<Object> keyDecoder = codec.getMapKeyDecoder();

  // ── encoder ──────────────────────────────────────────────────────────────

  @Test
  void encoder_plainStringKey() throws Exception {
    ByteBuf buf = keyEncoder.encode("42");
    byte[] bytes = new byte[buf.readableBytes()];
    buf.readBytes(bytes);
    assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("42");
    buf.release();
  }

  @Test
  void encoder_tenantAwareCacheKey() throws Exception {
    TenantAwareKey.CacheKey key = new TenantAwareKey.CacheKey(123L, "tenantA");
    ByteBuf buf = keyEncoder.encode(key);
    byte[] bytes = new byte[buf.readableBytes()];
    buf.readBytes(bytes);
    // CacheKey.toString() = "123:tenantA"
    assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("123:tenantA");
    buf.release();
  }

  // ── decoder – regression guard ───────────────────────────────────────────

  @Test
  void decoder_plainKey_returnsFullString() throws Exception {
    ByteBuf buf = Unpooled.wrappedBuffer("42".getBytes(StandardCharsets.UTF_8));
    Object decoded = keyDecoder.decode(buf, null);
    assertThat(decoded).isEqualTo("42");
  }

  @Test
  void decoder_keyContainingColon_returnsFullString() throws Exception {
    // REGRESSION: old decoder did substring(pos+1) which turned "123:tenantA" -> "tenantA"
    ByteBuf buf = Unpooled.wrappedBuffer("123:tenantA".getBytes(StandardCharsets.UTF_8));
    Object decoded = keyDecoder.decode(buf, null);
    assertThat(decoded).isEqualTo("123:tenantA");  // must NOT be just "tenantA"
  }

  @Test
  void decoder_keyWithMultipleColons_returnsFullString() throws Exception {
    // E.g. UUID-style key with colon in tenantId
    ByteBuf buf = Unpooled.wrappedBuffer("key:ten:ant".getBytes(StandardCharsets.UTF_8));
    Object decoded = keyDecoder.decode(buf, null);
    assertThat(decoded).isEqualTo("key:ten:ant");
  }

  // ── round-trip ───────────────────────────────────────────────────────────

  @Test
  void roundTrip_plainString() throws Exception {
    String original = "99";
    ByteBuf encoded = keyEncoder.encode(original);
    Object decoded = keyDecoder.decode(encoded, null);
    assertThat(decoded).isEqualTo(original);
  }

  @Test
  void roundTrip_tenantKey() throws Exception {
    TenantAwareKey.CacheKey key = new TenantAwareKey.CacheKey(7L, "tenant42");
    ByteBuf encoded = keyEncoder.encode(key);
    Object decoded = keyDecoder.decode(encoded, null);
    // The decoded value is the string representation used as the Redis field name
    assertThat(decoded).isEqualTo("7:tenant42");
  }
}
