package io.ebean.redis.encode;

import io.ebeaninternal.server.cache.CachedBeanData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class EncodeBeanData implements Encode {

  @Override
  public byte[] encode(Object value) {

    try {
      CachedBeanData data = (CachedBeanData) value;
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(os);

      data.writeExternal(oos);
      oos.flush();
      oos.close();

      return os.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to decode cache data", e);
    }
  }

  @Override
  public Object decode(byte[] data) {

    try {
      ByteArrayInputStream is = new ByteArrayInputStream(data);
      ObjectInputStream ois = new ObjectInputStream(is);

      CachedBeanData read = new CachedBeanData();
      read.readExternal(ois);
      return read;

    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException("Failed to decode cache data", e);
    }
  }
}
