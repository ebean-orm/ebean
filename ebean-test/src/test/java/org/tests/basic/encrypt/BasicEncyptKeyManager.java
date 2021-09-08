package org.tests.basic.encrypt;

import io.ebean.config.EncryptKey;
import io.ebean.config.EncryptKeyManager;

public class BasicEncyptKeyManager implements EncryptKeyManager {

  /**
   * Initialise the key manager.
   */
  @Override
  public void initialise() {

  }

  @Override
  public EncryptKey getEncryptKey(String tableName, String columnName) {
    // Must be 16 Chars for Oracle function
    return new BasicEncryptKey("simple0123456789");
  }

}
