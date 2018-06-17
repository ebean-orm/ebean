package io.ebeaninternal.api;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Context used to write binary message (like RemoteTransactionEvent).
 */
public class BinaryWriteContext {

  private final DataOutputStream out;

  private long counter;

  public BinaryWriteContext(DataOutputStream out) {
    this.out = out;
  }

  /**
   * Return the number of message parts that have been written.
   */
  public long counter() {
    return counter;
  }

  /**
   * Return the output stream to write to.
   */
  public DataOutputStream os() {
    return out;
  }

  /**
   * Start a message part with a given type code.
   */
  public DataOutputStream start(int type) throws IOException {
    counter++;
    out.writeBoolean(true);
    out.writeInt(type);
    return out;
  }

  /**
   * End of message parts.
   */
  public void end() throws IOException {
    out.writeBoolean(false);
  }
}
