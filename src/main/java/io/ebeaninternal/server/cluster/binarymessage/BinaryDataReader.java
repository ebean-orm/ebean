package io.ebeaninternal.server.cluster.binarymessage;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.TransactionEventTable;
import io.ebeaninternal.server.cache.RemoteCacheEvent;
import io.ebeaninternal.server.cluster.MessageServerProvider;
import io.ebeaninternal.server.transaction.BeanPersistIds;
import io.ebeaninternal.server.transaction.RemoteTableMod;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Reads the binary message returning RemoteTransactionEvent.
 */
class BinaryDataReader {

  private final MessageServerProvider serverProvider;
  private final DataInputStream dataInput;

  private SpiEbeanServer server;
  private RemoteTransactionEvent event;

  BinaryDataReader(MessageServerProvider serverProvider, byte[] data) {
    this.serverProvider = serverProvider;
    this.dataInput = new DataInputStream(new ByteArrayInputStream(data));
  }

  /**
   * Read the binary message returning a RemoteTransactionEvent.
   */
  RemoteTransactionEvent read() throws IOException {

    String serverName = dataInput.readUTF();

    this.server = (SpiEbeanServer) serverProvider.getServer(serverName);
    if (server == null) {
      throw new IllegalStateException("EbeanServer not found for name [" + serverName + "]");
    }
    this.event = new RemoteTransactionEvent(server);
    boolean more = dataInput.readBoolean();
    while (more) {
      readMessage();
      more = dataInput.readBoolean();
    }
    return event;
  }

  private void readMessage() throws IOException {

    int msgType = dataInput.readInt();
    switch (msgType) {
      case BinaryMessage.TYPE_BEANIUD:
        event.addBeanPersistIds(BeanPersistIds.readBinaryMessage(server, dataInput));
        break;

      case BinaryMessage.TYPE_TABLEIUD:
        event.addTableIUD(TransactionEventTable.TableIUD.readBinaryMessage(dataInput));
        break;

      case BinaryMessage.TYPE_CACHE:
        event.addRemoteCacheEvent(RemoteCacheEvent.readBinaryMessage(dataInput));
        break;

      case BinaryMessage.TYPE_TABLEMOD:
        event.addRemoteTableMod(RemoteTableMod.readBinaryMessage(dataInput));
        break;

      default:
        throw new RuntimeException("Invalid Transaction msgType " + msgType);
    }
  }
}
