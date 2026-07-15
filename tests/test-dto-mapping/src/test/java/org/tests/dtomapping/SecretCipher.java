package org.tests.dtomapping;

/**
 * Stand-in for a conversion needing a real dependency (e.g. {@code AES256Cipher}) - the
 * {@code @DtoConvert} instance-dispatch case (requirement r14): an instance is resolved via
 * {@code DtoConverterManager.get(SecretCipher.class)}, which must be registered (see
 * {@link ContactConversionDbConfigProvider}) before the {@code Database} is built.
 */
public interface SecretCipher {

  String decode(String encoded);
}
