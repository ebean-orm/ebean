package com.avaje.ebeaninternal.server.cluster;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents the contents sent as a single DatagramPacket.
 * <p>
 * The contents is typically multiple messages (ACK,PING etc) or all or part of
 * a RemoteTransactionEvent.
 * </p>
 * <p>
 * Due to the hard limit on the size of UDP packets a RemoteTransactionEvent
 * with lots of information could be broken up into multiple packets.
 * </p>
 * 
 * @author rbygrave
 */
public class Packet {
    
    /**
     * A Packet that holds protocol messages like ACK, PING etc. 
     */
    public static final short TYPE_MESSAGES = 1;
    
    /**
     * A Packet that holds TransactionEvent information such as Bean
     * and or Table IUD information.
     */
    public static final short TYPE_TRANSEVENT = 2;

    /**
     * The type of Packet.
     */
    protected short packetType;
    
    /**
     * The PacketId.
     */
    protected long packetId;
    
    /**
     * The timestamp the Packet was created.
     */
    protected long timestamp;
    
    /**
     * The EbeanServer name this relates to if relevant.
     */
    protected String serverName;
    
    protected ByteArrayOutputStream buffer;
    protected DataOutputStream dataOut;
    protected byte[] bytes;
    
    /**
     * The number of messages in this Packet.
     */
    private int messageCount;
    
    /**
     * The number of times this Packet was resent.
     */
    private int resendCount;

    /**
     * Create a Packet for writing messages to.
     */
    public static Packet forWrite(short packetType, long packetId, long timestamp, String serverName) throws IOException {
        return new Packet(true, packetType, packetId, timestamp, serverName);
    }
    
    /**
     * Create a Packet just reading the Header information.
     */
    public static Packet readHeader(DataInput dataInput) throws IOException {
        
        short packetType = dataInput.readShort();
        long packetId = dataInput.readLong();
        long timestamp = dataInput.readLong();
        String serverName = dataInput.readUTF();
        
        return new Packet(false, packetType, packetId, timestamp, serverName);
    }
    
    protected Packet(boolean write, short packetType, long packetId, long timestamp, String serverName) throws IOException{
        this.packetType = packetType;
        this.packetId = packetId;
        this.timestamp = timestamp;
        this.serverName = serverName;
        if (write){
            this.buffer = new ByteArrayOutputStream();
            this.dataOut = new DataOutputStream(buffer);
            writeHeader();
        } else {
            this.buffer = null;
            this.dataOut = null;
        }
    }

    private void writeHeader() throws IOException {
        dataOut.writeShort(packetType);
        dataOut.writeLong(packetId);
        dataOut.writeLong(timestamp);
        dataOut.writeUTF(serverName);
    }

    public int incrementResendCount() {
        return resendCount++;
    }
    
    public short getPacketType() {
        return packetType;
    }

    public long getPacketId() {
        return packetId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }

    public String getServerName() {
        return serverName;
    }
    
    public void writeEof() throws IOException {
        dataOut.writeBoolean(false);
    }
    
    public void read(DataInput dataInput) throws IOException {
        boolean more = dataInput.readBoolean();
        while (more){
            int msgType = dataInput.readInt();
            readMessage(dataInput, msgType);
            // see if there is more information
            more = dataInput.readBoolean();
        }
    }
    
    /**
     * Overridden by more specific Packet implementations to read the messages.
     */
    protected void readMessage(DataInput dataInput, int msgType) throws IOException {
        
    }

    /**
     * Write a binary message to this packet returning true if there was
     * enough room to do so. Return false if the message was too large for
     * the remaining space left - in this case another Packet should be
     * created to put that message into.
     */
    public boolean writeBinaryMessage(BinaryMessage msg, int maxPacketSize) throws IOException {
        
        byte[] bytes = msg.getByteArray();
        
        if (messageCount > 0 && (bytes.length + buffer.size() > maxPacketSize)){
            // we are actually going to ignore the maxPacketSize iff we have one
            // large message. 
            
            // false = no more messages
            dataOut.writeBoolean(false);
            return false;
        }
        ++messageCount;
        // true = another message follows
        dataOut.writeBoolean(true);
        dataOut.write(bytes);
        return true;
    }

    public int getSize() {
        return getBytes().length;
    }
    
    /**
     * Return the Packet as raw bytes.
     */
    public byte[] getBytes() {
        if (bytes == null){
            bytes = buffer.toByteArray();
            buffer = null;
            dataOut = null;
        }
        return bytes;
    }

    
}
