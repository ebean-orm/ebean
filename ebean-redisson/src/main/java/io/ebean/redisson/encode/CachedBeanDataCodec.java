package io.ebean.redisson.encode;

import io.ebeaninternal.server.cache.CachedBeanData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CachedBeanDataCodec extends CacheCodec {

    private final Encoder encoder = in -> {
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        try (ByteBufOutputStream os = new ByteBufOutputStream(out);
             ObjectOutputStream oos = new ObjectOutputStream(os)) {
            ((CachedBeanData) in).writeExternal(oos);
            return os.buffer();
        } catch (IOException e) {
            out.release();
            throw e;
        } catch (Exception e) {
            out.release();
            throw new IOException(e);
        }
    };
    private final Decoder<Object> decoder = (in, state) -> {

        try (ByteBufInputStream is = new ByteBufInputStream(in);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            CachedBeanData data = new CachedBeanData();
            data.readExternal(ois);
            return data;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    };

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }
}
