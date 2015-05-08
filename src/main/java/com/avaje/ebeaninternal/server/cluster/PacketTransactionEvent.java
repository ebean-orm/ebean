package com.avaje.ebeaninternal.server.cluster;

import java.io.DataInput;
import java.io.IOException;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.transaction.BeanDelta;
import com.avaje.ebeaninternal.server.transaction.BeanPersistIds;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;

/**
 * A Packet holding TransactionEvent data.
 * <p>
 * Due to the hard limit for UDP packet sizes a RemoteTransactionEvent
 * is actually broken up into smaller messages.
 * </p>
 */
public class PacketTransactionEvent extends Packet {

    private final SpiEbeanServer server;
    
    private final RemoteTransactionEvent event;

    public static PacketTransactionEvent forWrite(long packetId, long timestamp, String serverName) throws IOException {
        return new PacketTransactionEvent(true, packetId, timestamp, serverName);
    }
    
    private PacketTransactionEvent(boolean write, long packetId, long timestamp, String serverName) throws IOException {
        super(write, TYPE_TRANSEVENT, packetId, timestamp, serverName);
        this.server = null;
        this.event = null;
    }

    private PacketTransactionEvent(Packet header, SpiEbeanServer server) throws IOException {
        super(false, TYPE_TRANSEVENT, header.packetId, header.timestamp, header.serverName);
        this.server = server;
        this.event = new RemoteTransactionEvent(server);
    }

    public static PacketTransactionEvent forRead(Packet header, SpiEbeanServer server) throws IOException {
        return new PacketTransactionEvent(header, server);
    }
 
    public RemoteTransactionEvent getEvent() {
        return event;
    }

    protected void readMessage(DataInput dataInput, int msgType) throws IOException {
        
        switch (msgType) {
        case BinaryMessage.TYPE_BEANIUD:
            event.addBeanPersistIds(BeanPersistIds.readBinaryMessage(server, dataInput));
            break;
            
        case BinaryMessage.TYPE_TABLEIUD:
            event.addTableIUD(TableIUD.readBinaryMessage(dataInput));
            break;

        case BinaryMessage.TYPE_BEANDELTA:
            event.addBeanDelta(BeanDelta.readBinaryMessage(server, dataInput));
            break;

        default:
            throw new RuntimeException("Invalid Transaction msgType "+msgType);
        }
    }
    
}
