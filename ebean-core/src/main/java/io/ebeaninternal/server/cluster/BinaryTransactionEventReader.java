package io.ebeaninternal.server.cluster;

import io.ebeaninternal.api.BinaryReadContext;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;

import java.io.IOException;

/**
 * Mechanism to convert RemoteTransactionEvent to/from byte[] content.
 */
public class BinaryTransactionEventReader {

  private final ServerLookup serverLookup;

  public BinaryTransactionEventReader(ServerLookup serverLookup) {
    this.serverLookup = serverLookup;
  }

  /**
   * Read Transaction from bytes.
   */
  public RemoteTransactionEvent read(byte[] byteData) throws IOException {
    return read(new BinaryReadContext(byteData));
  }

  /**
   * Read Transaction using BinaryReadContext.
   */
  public RemoteTransactionEvent read(BinaryReadContext dataInput) throws IOException {

    String serverName = dataInput.readUTF();
    SpiEbeanServer server = (SpiEbeanServer) serverLookup.getServer(serverName);
    if (server == null) {
      throw new IllegalStateException("EbeanServer not found for name [" + serverName + "]");
    }
    RemoteTransactionEvent event =  new RemoteTransactionEvent(server);
    event.readBinary(dataInput);
    return event;
  }
}
