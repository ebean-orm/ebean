package com.avaje.ebeaninternal.server.cluster.mcast;

import java.io.IOException;

import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;

public interface Message {

    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException;

    public boolean isControlMessage();
    
    public String getToHostPort();
}
