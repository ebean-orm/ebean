package com.avaje.ebeaninternal.server.cluster.mcast;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.cluster.BinaryMessage;
import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;

public class MessageResend implements Message {

    private final String toHostPort;
    
    private final List<Long> resendPacketIds;

    public MessageResend(String toHostPort, List<Long> resendPacketIds) {
        this.toHostPort = toHostPort;
        this.resendPacketIds = resendPacketIds;
    }
    
    public MessageResend(String toHostPort) {
        this(toHostPort, new ArrayList<Long>(4));
    }

    public String toString() {
        return "Resend "+toHostPort+" "+resendPacketIds;
    }
    
    public boolean isControlMessage() {
        return false;
    }

    public String getToHostPort() {
        return toHostPort;
    }

    public void add(long packetId){
        resendPacketIds.add(Long.valueOf(packetId));
    }
    
    public List<Long> getResendPacketIds() {
        return resendPacketIds;
    }

    public static MessageResend readBinaryMessage(DataInput dataInput) throws IOException {

        String hostPort = dataInput.readUTF();
        
        MessageResend msg = new MessageResend(hostPort);
        
        int numberOfPacketIds = dataInput.readInt();
        for (int i = 0; i < numberOfPacketIds; i++) {
            long packetId = dataInput.readLong();
            msg.add(packetId);
        }
        
        return msg;
    }

    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {
        
        BinaryMessage m = new BinaryMessage(toHostPort.length() * 2 + 20);
        
        DataOutputStream os = m.getOs();
        os.writeInt(BinaryMessage.TYPE_MSGRESEND);
        os.writeUTF(toHostPort);
        os.writeInt(resendPacketIds.size());
        for (int i = 0; i < resendPacketIds.size(); i++) {
            Long packetId = resendPacketIds.get(i);
            os.writeLong(packetId.longValue());
        }
        os.flush();
        msgList.add(m);
    }
}
