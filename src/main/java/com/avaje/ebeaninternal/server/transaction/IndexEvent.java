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
