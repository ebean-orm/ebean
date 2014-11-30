package com.avaje.ebeaninternal.server.cluster;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mechanism to convert RemoteTransactionEvent to/from byte[] content.
 */
public abstract class SerialiseTransactionHelper {

  private static final Logger logger = LoggerFactory.getLogger(SerialiseTransactionHelper.class);

  private final PacketWriter packetWriter;

  public SerialiseTransactionHelper() {
    packetWriter = new PacketWriter(Integer.MAX_VALUE);
  }

  public abstract SpiEbeanServer getEbeanServer(String serverName);

  /**
   * Convert the RemoteTransactionEvent to byte[] content.
   */
  public DataHolder createDataHolder(RemoteTransactionEvent transEvent) throws IOException {

    List<Packet> packetList = packetWriter.write(transEvent);
    if (packetList.size() != 1) {
      throw new RuntimeException("Always expecting 1 Packet but got " + packetList.size());
    }
    byte[] data = packetList.get(0).getBytes();
    return new DataHolder(data);
  }

  /**
   * Convert the byte[] content to RemoteTransactionEvent.
   */
  public RemoteTransactionEvent read(DataHolder dataHolder) throws IOException {

    ByteArrayInputStream bi = new ByteArrayInputStream(dataHolder.getData());
    DataInputStream dataInput = new DataInputStream(bi);

    Packet header = Packet.readHeader(dataInput);

    SpiEbeanServer server = getEbeanServer(header.getServerName());
    if (server == null) {
      logger.error("server [{}] not found/registered?", header.getServerName());
    }

    PacketTransactionEvent tranEventPacket = PacketTransactionEvent.forRead(header, server);
    tranEventPacket.read(dataInput);
    return tranEventPacket.getEvent();
  }
}
