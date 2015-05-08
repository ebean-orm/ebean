package com.avaje.ebeaninternal.server.cluster.mcast;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import com.avaje.ebeaninternal.server.cluster.BinaryMessage;
import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;

public class MessageAck implements Message {

    private final String toHostPort;
    
    private final long gotAllPacketId;

    public MessageAck(String toHostPort, long gotAllPacketId) {
        this.toHostPort = toHostPort;
        this.gotAllPacketId = gotAllPacketId;
    }

    public String toString() {
        return "Ack "+toHostPort+" "+gotAllPacketId;
    }
    
    public boolean isControlMessage() {
        return false;
    }

    public String getToHostPort() {
        return toHostPort;
    }

    public long getGotAllPacketId() {
        return gotAllPacketId;
    }
    
    
    public static MessageAck readBinaryMessage(DataInput dataInput) throws IOException {

        String hostPort = dataInput.readUTF();
        long gotAllPacketId = dataInput.readLong();
        return new MessageAck(hostPort, gotAllPacketId);
    }

    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {
        
        BinaryMessage m = new BinaryMessage(toHostPort.length() * 2 + 20);
        
        DataOutputStream os = m.getOs();
        os.writeInt(BinaryMessage.TYPE_MSGACK);
        os.writeUTF(toHostPort);
        os.writeLong(gotAllPacketId);
        os.flush();
        
        msgList.add(m);
    }
}
