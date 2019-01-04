package io.ebeaninternal.api;

import java.io.IOException;

/**
 * Messages that can be sent in binary form.
 * <p>
 * Mainly RemoteTransactionEvent which is sent to cluster members.
 * </p>
 */
public interface BinaryWritable {

  int TYPE_BEANIUD = 1;
  int TYPE_TABLEIUD = 2;
  int TYPE_CACHE = 3;
  int TYPE_TABLEMOD = 4;

  /**
   * Write message in binary format.
   */
  void writeBinary(BinaryWriteContext out) throws IOException;

}
