package io.ebeaninternal.server.cluster.binarymessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The message broadcast around the cluster.
 */
public class ClusterMessage {

  private static final int MAX_LENGTH = 10 * 1024 * 1024;

  private final String registerIp;

  private final String podName;

  private final boolean register;

  private final byte[] data;

  /**
   * Create a register message.
   */
  public static ClusterMessage register(String registerIp, boolean register, String podName) {
    return new ClusterMessage(registerIp, register, podName);
  }

  /**
   * Create a transaction message.
   */
  public static ClusterMessage transEvent(byte[] data) {
    return new ClusterMessage(data);
  }

  /**
   * Create for register online/offline message.
   */
  private ClusterMessage(String registerIp, boolean register, String podName) {
    this.registerIp = registerIp;
    this.register = register;
    this.podName = podName;
    this.data = null;
  }

  /**
   * Create for a transaction message.
   */
  private ClusterMessage(byte[] data) {
    this.data = data;
    this.registerIp = null;
    this.podName = null;
    this.register = false;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (registerIp != null) {
      sb.append("register ");
      sb.append(register);
      sb.append(" ");
      sb.append(registerIp);
    } else {
      sb.append("[data]");
    }
    return sb.toString();
  }

  /**
   * Return true if this is a register event as opposed to a transaction message.
   */
  public boolean isRegisterEvent() {
    return registerIp != null;
  }

  /**
   * Return the register host for online/offline message.
   */
  public String getRegisterIp() {
    return registerIp;
  }

  public String getPodName() {
    return podName;
  }

  /**
   * Return true if register is true for a online/offline message.
   */
  public boolean isRegister() {
    return register;
  }

  /**
   * Return the raw message data.
   */
  public byte[] getData() {
    return data;
  }

  /**
   * Write the message in binary form.
   */
  public void write(DataOutputStream dataOutput) throws IOException {

    if (data != null) {
      // write data message
      dataOutput.writeInt(MsgKeys.DATA);
      dataOutput.writeInt(data.length);
      dataOutput.write(data);
    } else {
      // write header message
      dataOutput.writeInt(MsgKeys.HEADER);
      dataOutput.writeUTF(getRegisterIp());
      dataOutput.writeBoolean(register);
      dataOutput.writeUTF(getPodName());
    }
    dataOutput.flush();
  }

  /**
   * Read the message from binary form.
   */
  public static ClusterMessage read(DataInputStream dataInput) throws IOException, InvalidMessageException {

    int key = dataInput.readInt();
    if (key == MsgKeys.DATA) {
      int length = dataInput.readInt();
      if (length > MAX_LENGTH) {
        throw new IOException("Message data too large length:"+length);
      }
      byte[] data = new byte[length];
      dataInput.readFully(data);
      return new ClusterMessage(data);

    } else if (key == MsgKeys.HEADER) {
      String host = dataInput.readUTF();
      boolean registered = dataInput.readBoolean();
      String podName = dataInput.readUTF();
      return new ClusterMessage(host, registered, podName);

    } else {
      throw new InvalidMessageException("Invalid message key:" + key);
    }
  }

}
