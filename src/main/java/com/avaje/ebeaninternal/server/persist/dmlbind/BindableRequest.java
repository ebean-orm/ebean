package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.DerivedRelationshipData;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

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
   * Register the value from a update GeneratedValue. This can only be set to
   * the bean property after the where clause has bean built.
   */
  void registerGeneratedVersion(Object value);

  /**
   * Return the original PersistRequest.
   */
  PersistRequestBean<?> getPersistRequest();

  void registerDerivedRelationship(DerivedRelationshipData assocBean);

  /**
   * Return the system current time in millis. This is expected to the same time used
   * by multiple generated properties for a single request.
   */
  long now();

}
