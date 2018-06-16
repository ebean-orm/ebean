package io.ebeaninternal.server.cluster.binarymessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Writes BinaryMessageList to DataHolder raw byte[].
 */
class BinaryDataWriter {

  private final BinaryMessageList messageList;

  private final ByteArrayOutputStream buffer;

  private final DataOutputStream dataOut;

  private final String serverName;

  BinaryDataWriter(String serverName, BinaryMessageList messageList) {
    this.serverName = serverName;
    this.messageList = messageList;
    this.buffer = new ByteArrayOutputStream(256);
    this.dataOut = new DataOutputStream(buffer);
  }

  /**
   * Write the message as raw byte[].
   */
  byte[] write() throws IOException {

    //TODO: Add Protocol version and "podId" to support pubsub cluster
    //dataOut.writeInt(MsgKeys.MESSAGE_KEY);
    dataOut.writeUTF(serverName);
    for (BinaryMessage msg : messageList.getList()) {
      write(msg);
    }
    dataOut.writeBoolean(false);
    dataOut.flush();
    return buffer.toByteArray();
  }

  private void write(BinaryMessage msg) throws IOException {

    dataOut.writeBoolean(true);
    dataOut.write(msg.getByteArray());
  }

}
