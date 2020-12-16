package io.ebean.redis;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Provides a modified base64 encoded UUID and shorter 12 character random unique value.
 * <p>
 * <h3>newId()</h3>
 * <p>
 * It produces a 22 character string that is a base64 encoded UUID with the +
 * and / characters replaced with - and _ so as to be URL safe without requiring
 * encoding.
 * </p>
 * <h3>newShortId()</h3>
 * <p>
 * It produces a 12 character string that base64 encoded random number (72 bit).
 * </p>
 * <p>
 * Note that this now internally uses java.util.Base64 to encode the values.
 * </p>
 */
public class ModId {

  private static final SecureRandom shortIdSecureRandom = new SecureRandom();

  private static final Base64.Encoder urlEncoder = Base64.getUrlEncoder();

  /**
   * Return a 12 character string using a 72 bit randomly generated ID encoded
   * in modified base64.
   * <p>
   * A UUID is 128 bits and this is 72 bits so quite a bit smaller but still
   * very random with one in 4.7 * 10^21 chance of a collision.
   * </p>
   */
  public static String id() {

    // Random 72 bits
    byte[] randomBytes = new byte[9];
    shortIdSecureRandom.nextBytes(randomBytes);
    return encode64(randomBytes);
  }

  private static String encode64(byte[] bytes) {
    return urlEncoder.encodeToString(bytes);
  }

}
