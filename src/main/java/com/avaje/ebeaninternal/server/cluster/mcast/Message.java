package com.avaje.ebeaninternal.server.cluster.mcast;

import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;

import java.io.IOException;

public interface Message {

  void writeBinaryMessage(BinaryMessageList msgList) throws IOException;

  boolean isControlMessage();

  String getToHostPort();
}
