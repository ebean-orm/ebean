package io.ebeaninternal.server.persist.dmlbind;

import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanProperty;

import java.sql.SQLException;

/**
 * Request object passed to bindables.
 */
public interface BindableRequest {

  /**
   * Set the id for use with summary level logging.
   */
  void setIdValue(Object idValue);

  /**
   * Bind the value to a PreparedStatement.
   * <p>
   * Takes into account logicalType to dbType conversion if required.
   * </p>
   * <p>
   * Returns the value that was bound (and was potentially converted from
   * logicalType to dbType.
   * </p>
   */
  void bind(Object value, BeanProperty prop) throws SQLException;

  /**
   * Bind a raw value. Used to bind the discriminator column.
   */
  void bind(Object value, int sqlType) throws SQLException;

  /**
   * Bind a raw value with a placeHolder to put into the transaction log.
   */
  void bindNoLog(Object value, int sqlType, String logPlaceHolder) throws SQLException;

  /**
   * Bind the value to the preparedStatement without logging.
   */
  void bindNoLog(Object value, BeanProperty prop) throws SQLException;

  /**
   * Return the original PersistRequest.
   */
  PersistRequestBean<?> getPersistRequest();

  /**
   * Return the system current time in millis. This is expected to the same time used
   * by multiple generated properties for a single request.
   */
  long now();

  /**
   * Return true if this is an update request.
   */
  boolean isUpdate();
}
