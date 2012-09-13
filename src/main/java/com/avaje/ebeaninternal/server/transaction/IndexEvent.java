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
package com.avaje.ebeaninternal.server.transaction;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import com.avaje.ebeaninternal.server.cluster.BinaryMessage;
import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;

public class IndexEvent {

    public static final int COMMIT_EVENT = 1;
    public static final int OPTIMISE_EVENT = 2;
    
    private final int eventType;
    private final String indexName;
    
    public IndexEvent(int eventType, String indexName) {
        this.eventType = eventType;
        this.indexName = indexName;
    }

    public int getEventType() {
        return eventType;
    }

    public String getIndexName() {
        return indexName;
    }
    
    public static IndexEvent readBinaryMessage(DataInput dataInput) throws IOException {
        
        int eventType = dataInput.readInt();
        String indexName = dataInput.readUTF();
        return new IndexEvent(eventType, indexName);
    }
    
    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {
        
        BinaryMessage msg = new BinaryMessage(indexName.length()+10);
        DataOutputStream os = msg.getOs();
        os.writeInt(BinaryMessage.TYPE_INDEX);
        os.writeInt(eventType);
        os.writeUTF(indexName);
        
        msgList.add(msg);
    }
    
}
