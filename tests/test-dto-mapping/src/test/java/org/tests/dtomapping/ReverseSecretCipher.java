package org.tests.dtomapping;

/**
 * Simple, deterministic (non-cryptographic) {@link SecretCipher} used purely to prove instance
 * dispatch actually goes through the resolved instance (rather than being coincidentally
 * static-callable) - reverses the encoded string.
 */
public final class ReverseSecretCipher implements SecretCipher {

  @Override
  public String decode(String encoded) {
    return new StringBuilder(encoded).reverse().toString();
  }
}
