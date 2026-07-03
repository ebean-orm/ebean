package io.ebean.redisson.encode;

import io.ebean.cache.TenantAwareKey;
import io.netty.buffer.Unpooled;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.nio.charset.StandardCharsets;

public abstract class CacheCodec extends BaseCodec {

    @Override
    public Encoder getMapKeyEncoder() {
        return in -> {
            try {
                if (!(in instanceof String) && !(in instanceof TenantAwareKey.CacheKey)) {
                    throw new IllegalStateException("Expecting String keys but got type: " + in.getClass());
                }
                byte[] bytes = in.toString().getBytes(StandardCharsets.UTF_8);
                return Unpooled.wrappedBuffer(bytes);
            } catch (Exception e) {
                throw new RuntimeException("Failed to encode cache key", e);
            }
        };
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return (buf, state) -> {

            try {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                String key = new String(bytes, StandardCharsets.UTF_8);
                int pos = key.indexOf(':');
                if (pos >= 0) {
                    return key.substring(pos + 1);
                }
                return key;
            } catch (Exception e) {
                throw new RuntimeException("Failed to decode cache key", e);
            }
        };
    }
}
