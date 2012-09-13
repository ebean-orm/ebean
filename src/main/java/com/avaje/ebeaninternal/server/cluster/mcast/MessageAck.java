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
