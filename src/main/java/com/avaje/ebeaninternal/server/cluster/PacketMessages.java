package com.avaje.ebeaninternal.server.cluster;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.cluster.mcast.Message;
import com.avaje.ebeaninternal.server.cluster.mcast.MessageAck;
import com.avaje.ebeaninternal.server.cluster.mcast.MessageControl;
import com.avaje.ebeaninternal.server.cluster.mcast.MessageResend;

/**
 * A Packet that contains Ack, Resend and Control messages.
 * 
 * @author rbygrave
 */
public class PacketMessages extends Packet {

    private final ArrayList<Message> messages;

    public static PacketMessages forWrite(long packetId, long timestamp, String serverName) throws IOException {
        return new PacketMessages(true, packetId, timestamp, serverName);
    }
    
    public static PacketMessages forRead(Packet header) throws IOException {
        return new PacketMessages(header);
    }
    
    private PacketMessages(boolean write, long packetId, long timestamp, String serverName) throws IOException {
        super(write, TYPE_MESSAGES, packetId, timestamp, serverName);
        this.messages = null;
    }

    private PacketMessages(Packet header) throws IOException {
        super(false, TYPE_MESSAGES, header.packetId, header.timestamp, header.serverName);
        this.messages = new ArrayList<Message>();
    }
    
    /**
     * Return the messages contained in this Packet.
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Read the messages (Ack, Resend or Control) contained in this packet.
     */
    protected void readMessage(DataInput dataInput, int msgType) throws IOException {
        
        switch (msgType) {
        case BinaryMessage.TYPE_MSGCONTROL:
            messages.add(MessageControl.readBinaryMessage(dataInput));
            break;

        case BinaryMessage.TYPE_MSGACK:
            messages.add(MessageAck.readBinaryMessage(dataInput));
            break;

        case BinaryMessage.TYPE_MSGRESEND:
            messages.add(MessageResend.readBinaryMessage(dataInput));
            break;

        default:
            throw new RuntimeException("Invalid Transaction msgType "+msgType);
        }
    }
}
