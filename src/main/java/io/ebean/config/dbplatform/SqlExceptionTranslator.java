package io.ebean.config.dbplatform;

import javax.persistence.PersistenceException;
import java.sql.SQLException;

/**
 * Used to translate SQLExceptions to specific persistence exceptions.
 */
public interface SqlExceptionTranslator {

  /**
   * Translate the given exception.
   */
  PersistenceException translate(String message, SQLException e);
}
