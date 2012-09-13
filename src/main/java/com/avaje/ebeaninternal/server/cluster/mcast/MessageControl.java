/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
