package io.ebean.redis.encode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class EncodeSerializable implements Encode {

  @Override
  public byte[] encode(Object value) {
    try {
      final var baos = new ByteArrayOutputStream();
      final var oos = new ObjectOutputStream(baos);
      oos.writeObject(value);
      oos.flush();
      oos.close();
      return baos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to decode cache data", e);
    }
  }

  @Override
  public Object decode(byte[] data) {
    try {
      try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
        return ois.readObject();
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException("Failed to decode cache data", e);
    }
  }
}
