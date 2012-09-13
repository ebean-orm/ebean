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

public class IndexInvalidate {

    private final String indexName;
    
    public IndexInvalidate(String indexName) {
        this.indexName = indexName;
    }

    public String getIndexName() {
        return indexName;
    }
    
    @Override
    public int hashCode() {
        int hc = IndexInvalidate.class.hashCode();
        hc = hc * 31 + indexName.hashCode();
        return hc;
    }
    
    @Override
    public boolean equals(Object o){
        if (o instanceof IndexInvalidate == false){
            return false;
        }
        return indexName.equals(((IndexInvalidate)o).indexName);
    }
    
    public static IndexInvalidate readBinaryMessage(DataInput dataInput) throws IOException {
        
        
        String indexName = dataInput.readUTF();
        return new IndexInvalidate(indexName);
    }
    
    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {
        
        BinaryMessage msg = new BinaryMessage(indexName.length()+10);
        DataOutputStream os = msg.getOs();
        os.writeInt(BinaryMessage.TYPE_INDEX_INVALIDATE);
        os.writeUTF(indexName);
        
        msgList.add(msg);
    }
    
}
