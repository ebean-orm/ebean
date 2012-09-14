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
