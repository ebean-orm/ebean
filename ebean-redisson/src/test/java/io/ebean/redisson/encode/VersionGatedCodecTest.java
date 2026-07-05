package io.ebean.redisson.encode;

import io.ebeaninternal.server.cache.CachedBeanData;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link VersionGatedCodec}.
 *
 * Verifies that:
 * <ul>
 *   <li>The encoder prepends the magic MARKER + 8-byte big-endian version</li>
 *   <li>The decoder strips the prefix before delegating to the inner codec</li>
 *   <li>The decoder tolerates absent marker (backward compat)</li>
 *   <li>The key encoder/decoder are delegated to the inner codec unchanged</li>
 * </ul>
 */
class VersionGatedCodecTest {

  private final CachedBeanDataCodec inner = new CachedBeanDataCodec();
  private final VersionGatedCodec codec = new VersionGatedCodec(inner);

  private final Encoder valueEncoder = codec.getValueEncoder();
  private final Decoder<Object> valueDecoder = codec.getValueDecoder();

  // ── marker structure ─────────────────────────────────────────────────────

  @Test
  void encoder_prependsMarkerAndVersion() throws Exception {
    CachedBeanData data = beanData(5L);
    ByteBuf encoded = valueEncoder.encode(data);

    // First 2 bytes must be the magic marker
    byte b0 = encoded.getByte(0);
    byte b1 = encoded.getByte(1);
    assertThat(b0).isEqualTo(VersionGatedCodec.MARKER[0]);
    assertThat(b1).isEqualTo(VersionGatedCodec.MARKER[1]);

    // Next 8 bytes are the version (big-endian long = 5)
    long version = encoded.getLong(2);
    assertThat(version).isEqualTo(5L);

    // Total length > PREFIX_BYTES
    assertThat(encoded.readableBytes()).isGreaterThan(VersionGatedCodec.PREFIX_BYTES);
    encoded.release();
  }

  @Test
  void encoder_zeroVersion_whenNoCachedBeanData() throws Exception {
    // Non-CachedBeanData value → version treated as 0
    CachedBeanDataCodec codec2 = new CachedBeanDataCodec();
    VersionGatedCodec gated = new VersionGatedCodec(codec2);

    CachedBeanData data = beanData(0L);
    ByteBuf encoded = gated.getValueEncoder().encode(data);
    long version = encoded.getLong(2);
    assertThat(version).isEqualTo(0L);
    encoded.release();
  }

  // ── round-trip ───────────────────────────────────────────────────────────

  @Test
  void roundTrip_versionedBeanData() throws Exception {
    CachedBeanData original = beanData(3L);
    ByteBuf encoded = valueEncoder.encode(original);
    Object decoded = valueDecoder.decode(encoded, null);

    assertThat(decoded).isInstanceOf(CachedBeanData.class);
    CachedBeanData result = (CachedBeanData) decoded;
    assertThat(result.getVersion()).isEqualTo(3L);
  }

  @Test
  void roundTrip_zeroVersion() throws Exception {
    CachedBeanData original = beanData(0L);
    ByteBuf encoded = valueEncoder.encode(original);
    Object decoded = valueDecoder.decode(encoded, null);

    assertThat(decoded).isInstanceOf(CachedBeanData.class);
    assertThat(((CachedBeanData) decoded).getVersion()).isEqualTo(0L);
  }

  @Test
  void decoder_toleratesAbsentMarker() throws Exception {
    // Data without the marker prefix (simulates data stored before VersionGatedCodec was added)
    CachedBeanData original = beanData(1L);
    ByteBuf rawEncoded = inner.getValueEncoder().encode(original);

    // Decoder must NOT throw and must still return a valid CachedBeanData
    Object decoded = valueDecoder.decode(rawEncoded, null);
    assertThat(decoded).isInstanceOf(CachedBeanData.class);
  }

  // ── key codec delegation ─────────────────────────────────────────────────

  @Test
  void keyEncoder_delegatesToInner() throws Exception {
    // VersionGatedCodec must delegate key encoding to the inner codec
    ByteBuf fromGated = codec.getMapKeyEncoder().encode("myKey");
    ByteBuf fromInner = inner.getMapKeyEncoder().encode("myKey");

    byte[] gatedBytes = new byte[fromGated.readableBytes()];
    fromGated.readBytes(gatedBytes);
    byte[] innerBytes = new byte[fromInner.readableBytes()];
    fromInner.readBytes(innerBytes);

    assertThat(gatedBytes).isEqualTo(innerBytes);
    fromGated.release();
    fromInner.release();
  }

  @Test
  void keyDecoder_delegatesToInner() throws Exception {
    ByteBuf buf = codec.getMapKeyEncoder().encode("someKey");
    Object decoded = codec.getMapKeyDecoder().decode(buf, null);
    assertThat(decoded).isEqualTo("someKey");
  }

  // ── helper ───────────────────────────────────────────────────────────────

  private static CachedBeanData beanData(long version) {
    return new CachedBeanData(null, null, java.util.Collections.emptyMap(), version);
  }
}
