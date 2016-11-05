package com.avaje.ebeaninternal.server.util;

import java.security.MessageDigest;

public class Md5 {

  /**
   * Return the MD5 hash of the underlying sql.
   */
  public static String hash(String content) {

    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(content.getBytes("UTF-8"));
      return digestToHex(digest);
    } catch (Exception e) {
      throw new RuntimeException("MD5 hashing failed", e);
    }
  }

  /**
   * Convert the digest into a hex value.
   */
  private static String digestToHex(byte[] digest) {

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < digest.length; i++) {
      sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.toString();
  }

}
