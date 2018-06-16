package io.ebeaninternal.server.cluster.binarymessage;

import io.ebeaninternal.server.cluster.MessageServerProvider;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;

import java.io.IOException;

/**
 * Mechanism to convert RemoteTransactionEvent to/from byte[] content.
 */
public class MessageReadWrite {

  private final MessageServerProvider serverProvider;

  public MessageReadWrite(MessageServerProvider serverProvider) {
    this.serverProvider = serverProvider;
  }

  /**
   * Convert the RemoteTransactionEvent to raw byte[] content.
   */
  public byte[] write(RemoteTransactionEvent transEvent) throws IOException {

    BinaryMessageList messageList = new BinaryMessageList();
    transEvent.writeBinaryMessage(messageList);

    return new BinaryDataWriter(transEvent.getServerName(), messageList).write();
  }

  /**
   * Convert the byte[] content to RemoteTransactionEvent.
   */
  public RemoteTransactionEvent read(byte[] data) throws IOException {

    return new BinaryDataReader(serverProvider, data).read();
  }
}
