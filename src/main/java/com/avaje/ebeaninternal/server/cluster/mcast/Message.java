package com.avaje.ebeaninternal.server.cluster.mcast;

import java.io.IOException;

import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;

public interface Message {

    void writeBinaryMessage(BinaryMessageList msgList) throws IOException;

    boolean isControlMessage();
    
    String getToHostPort();
}
