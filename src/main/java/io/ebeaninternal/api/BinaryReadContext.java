package io.ebeaninternal.api;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Context used to read binary format messages.
 */
public class BinaryReadContext {

  private final DataInputStream in;

  /**
   * Create with protocol 0 and byte data.
   */
  public BinaryReadContext(byte[] byteData) {
    this(new DataInputStream(new ByteArrayInputStream(byteData)));
  }

  /**
   * Create with protocol version and DataInputStream data.
   */
  public BinaryReadContext(DataInputStream in) {
    this.in = in;
  }

  public DataInputStream in() {
    return in;
  }

  public boolean readBoolean() throws IOException {
    return in.readBoolean();
  }

  public int readInt() throws IOException {
    return in.readInt();
  }

  public String readUTF() throws IOException {
    return in.readUTF();
  }

  public long readLong() throws IOException {
    return in.readLong();
  }
}
