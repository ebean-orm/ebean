package io.ebeaninternal.util;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;


/**
 * Utilities for encoding and decoding strings.
 */
public final class EncodeUtil {

  private EncodeUtil() {
    /* no instances */
  }

  /**
   * URL-encodes the specified UTF-8 string.
   */
  public static String urlEncode(String string) {
    if (string == null) return null;
    try {
      return URLEncoder.encode(string, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("Support for UTF-8 is mandated by the Java spec", e);
    }
  }

  /**
   * URL-decodes the specified string as a UTF-8 string.
   */
  public static String urlDecode(String string) {
    if (string == null) return null;
    try {
      return URLDecoder.decode(string, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("Support for UTF-8 is mandated by the Java spec", e);
    }
  }

  /**
   * Returns the bytes corresponding to the specified ASCII string.
   */
  public static byte[] asciiToBytes(String string) {
    if (string == null) return null;
    try {
      return string.getBytes("US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("Support for US-ASCII is mandated by the Java spec", e);
    }
  }

  /**
   * Returns the ASCII string corresponding to the specified bytes.
   */
  public static String bytesToAscii(byte[] data) {
    try {
      return new String(data, "US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("Support for US-ASCII is mandated by the Java spec", e);
    }
  }

  /**
   * Returns the bytes corresponding to the specified UTF-8 string.
   */
  public static byte[] utf8ToBytes(String string) {
    if (string == null) return null;
    try {
      return string.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("Support for UTF-8 is mandated by the Java spec", e);
    }
  }

  /**
   * Returns the UTF-8 string corresponding to the specified bytes.
   */
  public static String bytesToUtf8(byte[] data) {
    try {
      return new String(data, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("Support for UTF-8 is mandated by the Java spec", e);
    }
  }

  /**
   * Returns the UTF-8 string corresponding to the specified bytes.
   */
  public static String decodeBytes(byte[] data, String encoding) {
    try {
      return new String(data, encoding);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Error decoding bytes with " + encoding, e);
    }
  }
}
