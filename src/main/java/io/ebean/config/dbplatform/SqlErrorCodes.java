package io.ebean.config.dbplatform;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to build a SQLCodeTranslator given DB platform specific codes.
 */
public class SqlErrorCodes {

  private Map<String,DataErrorType> map = new HashMap<>();

  /**
   * Map the codes to AcquireLockException.
   */
  public SqlErrorCodes addAcquireLock(String... codes) {
    return add(DataErrorType.AcquireLock, codes);
  }

  /**
   * Map the codes to DataIntegrityException.
   */
  public SqlErrorCodes addDataIntegrity(String... codes) {
    return add(DataErrorType.DataIntegrity, codes);
  }

  /**
   * Map the codes to DuplicateKeyException.
   */
  public SqlErrorCodes addDuplicateKey(String... codes) {
    return add(DataErrorType.DuplicateKey, codes);
  }

  private SqlErrorCodes add(DataErrorType type, String... codes) {
    for (String code : codes) {
      map.put(code, type);
    }
    return this;
  }

  /**
   * Build and return the SQLCodeTranslator with the mapped codes.
   */
  public SqlCodeTranslator build() {
    return new SqlCodeTranslator(map);
  }
}
