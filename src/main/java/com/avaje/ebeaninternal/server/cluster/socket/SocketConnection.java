package com.avaje.ebeaninternal.server.cluster.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * The client side of a TCP Sockect connection.
 */
class SocketConnection {

  /**
   * The underlying ObjectInputStream.
   */
  ObjectInputStream ois;

  /**
   * The underlying inputStream.
   */
  InputStream is;

  /**
   * The underlying outputStream.
   */
  OutputStream os;

  /**
   * The underlying socket.
   */
  Socket socket;

  /**
   * Create for a given Socket.
   */
  public SocketConnection(Socket socket) throws IOException {
    this.is = socket.getInputStream();
    this.os = socket.getOutputStream();
    this.socket = socket;
  }

  /**
   * Disconnect from the server.
   */
  public void disconnect() throws IOException {
    os.flush();
    socket.close();
  }

  /**
   * Flush the outputStream.
   */
  public void flush() throws IOException {
    os.flush();
  }

  /**
   * Read an object from the object input stream.
   */
  public Object readObject() throws IOException, ClassNotFoundException {
    return getObjectInputStream().readObject();
  }

  /**
   * Get the object input stream.
   */
  public ObjectInputStream getObjectInputStream() throws IOException {
    if (ois == null) {
      ois = new ObjectInputStream(is);
    }
    return ois;
  }

}
