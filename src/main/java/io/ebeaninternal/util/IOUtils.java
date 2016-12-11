package io.ebeaninternal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 * Utilities for IO.
 */
public class IOUtils {

  /**
   * Reads the entire contents of the specified input stream and returns them
   * as a byte array.
   */
  public static byte[] read(InputStream in) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    pump(in, buffer);
    return buffer.toByteArray();
  }

  /**
   * Reads the entire contents of the specified input stream and returns them
   * as an ASCII string.
   */
  public static String readAscii(InputStream in) throws IOException {
    return EncodeUtil.bytesToAscii(read(in));
  }

  /**
   * Reads the entire contents of the specified input stream and returns them
   * as UTF-8 string.
   */
  public static String readUtf8(InputStream in) throws IOException {
    return EncodeUtil.bytesToUtf8(read(in));
  }

  /**
   * Reads the entire contents of the specified input stream and returns them
   * as a string using the encoding supplied.
   */
  public static String readEncoded(InputStream in, String encoding) throws IOException {
    return EncodeUtil.decodeBytes(read(in), encoding);
  }

  /**
   * Read the entire contents from the reader returning as a String.
   */
  public static String read(Reader reader) throws IOException {

    StringBuilder sb = new StringBuilder();
    try {
      char[] buffer = new char[4096];
      for (; ; ) {
        int len = reader.read(buffer);
        if (len < 0) {
          break;
        }
        sb.append(buffer, 0, len);
      }
    } finally {
      reader.close();
    }

    return sb.toString();
  }

  /**
   * Reads data from the specified input stream and copies it to the specified
   * output stream, until the input stream is at EOF. Both streams are then
   * closed.
   *
   * @throws IOException if the input or output stream is <code>null</code>
   */
  public static void pump(InputStream in, OutputStream out) throws IOException {

    if (in == null) throw new IOException("Input stream is null");
    if (out == null) throw new IOException("Output stream is null");

    try {
      try {
        byte[] buffer = new byte[4096];
        for (; ; ) {
          int bytes = in.read(buffer);
          if (bytes < 0) {
            break;
          }
          out.write(buffer, 0, bytes);
        }
      } finally {
        in.close();
      }
    } finally {
      out.close();
    }
  }
}
