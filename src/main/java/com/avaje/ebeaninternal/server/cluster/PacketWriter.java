package com.avaje.ebeaninternal.server.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.cluster.mcast.Message;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;

/**
 * Creates Packets for either RemoteTransactionEvents or Messages (Ping, ACK,
 * Join, Leave etc).
 * 
 * @author rbygrave
 */
public class PacketWriter {

    private final PacketIdGenerator idGenerator;
    private final PacketBuilder messagesPacketBuilder;
    private final PacketBuilder transEventPacketBuilder;

    /**
     * Create a PacketWriter with an expected max packet size.
     * <p>
     * In theory we would prefer to create packets up to the MTU size which for
     * Ethernet will likely be 1500. Note that the maxPacketSize is ignored for
     * large single messages.
     * </p>
     */
    public PacketWriter(int maxPacketSize) {
        this.idGenerator = new PacketIdGenerator();
        this.messagesPacketBuilder = new PacketBuilder(maxPacketSize, idGenerator, new MessagesPacketFactory());
        this.transEventPacketBuilder = new PacketBuilder(maxPacketSize, idGenerator, new TransPacketFactory());
    }

    /**
     * Return the currentPacketId.
     */
    public long currentPacketId() {
        return idGenerator.currentPacketId();
    }
    
    /**
     * Create Packets for a given list of messages.
     * <p>
     * Typically this creates a single Packet but there is a hard limit for UDP
     * packet sizes.
     * </p>
     */
    public List<Packet> write(boolean requiresAck, List<? extends Message> messages) throws IOException {

        BinaryMessageList binaryMsgList = new BinaryMessageList();
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            message.writeBinaryMessage(binaryMsgList);
        }
        return messagesPacketBuilder.write(requiresAck, binaryMsgList, "");
    }

    /**
     * Create Packets for a given RemoteTransactionEvent.
     * <p>
     * Typically this creates a single Packet but there is a hard limit for UDP
     * packet sizes.
     * </p>
     */
    public List<Packet> write(RemoteTransactionEvent transEvent) throws IOException {

        BinaryMessageList messageList = new BinaryMessageList();

        // split into reasonably small independent messages
        transEvent.writeBinaryMessage(messageList);

        return transEventPacketBuilder.write(true, messageList, transEvent.getServerName());
    }

    /**
     * Reuse the same packetIdCounter for building Packets for both Message and
     * RemoteTransactionEvent
     */
    private static class PacketIdGenerator {

        long packetIdCounter;

        public long nextPacketId() {
            return ++packetIdCounter;
        }
        
        public long currentPacketId() {
            return packetIdCounter;
        }
        
    }

    interface PacketFactory {

        public Packet createPacket(long packetId, long timestamp, String serverName) throws IOException;
    }

    private static class TransPacketFactory implements PacketFactory {

        public Packet createPacket(long packetId, long timestamp, String serverName) throws IOException {
            return PacketTransactionEvent.forWrite(packetId, timestamp, serverName);
        }
    }

    private static class MessagesPacketFactory implements PacketFactory {

        public Packet createPacket(long packetId, long timestamp, String serverName) throws IOException {
            return PacketMessages.forWrite(packetId, timestamp, serverName);
        }
    }

    /**
     * Helper class for building Packets from messages or
     * RemoteTransactionEvents.
     */
    private static class PacketBuilder {

        private final PacketIdGenerator idGenerator;
        private final PacketFactory packetFactory;
        private final int maxPacketSize;

        private PacketBuilder(int maxPacketSize, PacketIdGenerator idGenerator, PacketFactory packetFactory) {
            this.maxPacketSize = maxPacketSize;
            this.idGenerator = idGenerator;
            this.packetFactory = packetFactory;
        }

        private List<Packet> write(boolean requiresAck, BinaryMessageList messageList, String serverName)
                throws IOException {

            List<BinaryMessage> list = messageList.getList();

            ArrayList<Packet> packets = new ArrayList<Packet>(1);

            long timestamp = System.currentTimeMillis();

            long packetId = requiresAck ? idGenerator.nextPacketId() : 0;
            Packet p = packetFactory.createPacket(packetId, timestamp, serverName);

            packets.add(p);

            for (int i = 0; i < list.size(); i++) {
                BinaryMessage binMsg = list.get(i);
                if (!p.writeBinaryMessage(binMsg, maxPacketSize)) {
                    // didn't fit into the package so put into another packet
                    packetId = requiresAck ? idGenerator.nextPacketId() : 0;
                    p = packetFactory.createPacket(packetId, timestamp, serverName);
                    packets.add(p);
                    p.writeBinaryMessage(binMsg, maxPacketSize);
                }
            }
            p.writeEof();

            return packets;

        }
    }
}
