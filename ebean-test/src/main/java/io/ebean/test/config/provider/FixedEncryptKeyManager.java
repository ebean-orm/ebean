package io.ebean.test.config.provider;

import io.ebean.config.EncryptKey;
import io.ebean.config.EncryptKeyManager;

class FixedEncryptKeyManager implements EncryptKeyManager {

  private final FixedEncryptKey key;

  FixedEncryptKeyManager(String fixedKey) {
    this.key = new FixedEncryptKey(fixedKey);
  }

  /**
   * Initialise the key manager.
   */
  @Override
  public void initialise() {

  }

  @Override
  public EncryptKey getEncryptKey(String tableName, String columnName) {
    return key;
  }

}
