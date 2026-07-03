package io.ebean.redisson.encode;

import io.ebeaninternal.server.cache.CachedBeanData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

/**
 * Wraps a bean codec and stores the entity {@code @Version} as a fixed big-endian prefix in front of the
 * encoded value, behind a 2-byte magic {@link #MARKER}.
 */
public class VersionGatedCodec extends BaseCodec {
    public static final byte[] MARKER = {(byte) 0xEB, (byte) 0x01};
    public static final int VERSION_BYTES = 8;
    public static final int PREFIX_BYTES = 2 + VERSION_BYTES;

    private final CacheCodec delegate;
    private final Encoder valueEncoder;
    private final Decoder<Object> valueDecoder;

    public VersionGatedCodec(CacheCodec delegate) {
        this.delegate = delegate;
        Encoder delegateEncoder = delegate.getValueEncoder();
        Decoder<Object> delegateDecoder = delegate.getValueDecoder();

        this.valueEncoder = in -> {
            long version = (in instanceof CachedBeanData) ? ((CachedBeanData) in).getVersion() : 0L;
            ByteBuf inner = delegateEncoder.encode(in);
            try {
                ByteBuf out = ByteBufAllocator.DEFAULT.buffer(PREFIX_BYTES + inner.readableBytes());
                out.writeBytes(MARKER);
                out.writeLong(version);
                out.writeBytes(inner);
                return out;
            } finally {
                inner.release();
            }
        };

        this.valueDecoder = (buf, state) -> {
            if (hasMarker(buf)) {
                buf.skipBytes(PREFIX_BYTES);
            }
            return delegateDecoder.decode(buf, state);
        };
    }

    private static boolean hasMarker(ByteBuf buf) {
        int ri = buf.readerIndex();
        if (buf.readableBytes() < PREFIX_BYTES) {
            return false;
        }
        for (int i = 0; i < MARKER.length; i++) {
            if (buf.getByte(ri + i) != MARKER[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Encoder getValueEncoder() {
        return valueEncoder;
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return valueDecoder;
    }

    @Override
    public Encoder getMapKeyEncoder() {
        return delegate.getMapKeyEncoder();
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return delegate.getMapKeyDecoder();
    }
}
