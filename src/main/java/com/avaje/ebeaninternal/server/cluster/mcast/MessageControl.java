package com.avaje.ebeaninternal.server.cluster.mcast;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import com.avaje.ebeaninternal.server.cluster.BinaryMessage;
import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;

public class MessageControl implements Message {

    public static final short TYPE_JOIN = 1;
    public static final short TYPE_LEAVE = 2;
    public static final short TYPE_PING = 3;
    public static final short TYPE_JOINRESPONSE = 7;
    public static final short TYPE_PINGRESPONSE = 8;
    
    private final short controlType;
    private final String fromHostPort;
    
    public static MessageControl readBinaryMessage(DataInput dataInput) throws IOException {
        short controlType = dataInput.readShort();
        String hostPort = dataInput.readUTF();
        return new MessageControl(controlType, hostPort);
    }
    
    public MessageControl(short controlType, String helloFromHostPort) {
        this.controlType = controlType;
        this.fromHostPort = helloFromHostPort;
    }


    public String toString() {
        switch (controlType) {
        case TYPE_JOIN: return "Join "+fromHostPort;
        case TYPE_LEAVE: return "Leave "+fromHostPort;
        case TYPE_PING: return "Ping "+fromHostPort;
        case TYPE_PINGRESPONSE: return "PingResponse "+fromHostPort;            

        default:
            throw new RuntimeException("Invalid controlType "+controlType);
        }
    }
    
    public boolean isControlMessage() {
        return true;
    }

    public short getControlType() {
        return controlType;
    }

    public String getToHostPort() {
        return "*";
    }
    
    public String getFromHostPort() {
        return fromHostPort;
    }

    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {
        
        BinaryMessage m = new BinaryMessage(fromHostPort.length() * 2 + 10);
        
        DataOutputStream os = m.getOs();
        os.writeInt(BinaryMessage.TYPE_MSGCONTROL);
        os.writeShort(controlType);
        os.writeUTF(fromHostPort);
        os.flush();
        
        msgList.add(m);
    }
}
