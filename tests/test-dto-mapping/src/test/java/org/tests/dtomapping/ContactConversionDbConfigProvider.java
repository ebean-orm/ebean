package org.tests.dtomapping;

import io.ebean.DatabaseBuilder;
import io.ebean.DtoConverterManager;
import io.ebean.config.DatabaseConfigProvider;

/**
 * Registers the {@link SecretCipher} instance needed by {@link ContactConversionDto}'s
 * {@code @DtoConvert} instance-dispatch conversion, via the standard
 * {@code DatabaseConfigProvider} ServiceLoader hook (see
 * {@code META-INF/services/io.ebean.config.DatabaseConfigProvider}) - which runs before the
 * {@code Database} (and so the generated {@code EbeanDtoMapperRegister}) is built, satisfying
 * {@code DtoConverterManager}'s "register before starting the Database" requirement regardless
 * of which test happens to trigger the default {@code Database}'s startup first.
 */
public class ContactConversionDbConfigProvider implements DatabaseConfigProvider {

  @Override
  public void apply(DatabaseBuilder config) {
    DtoConverterManager.put(SecretCipher.class, new ReverseSecretCipher());
  }
}
