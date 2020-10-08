package io.ebeaninternal.server.dto;

import io.ebeaninternal.server.type.DataReader;

import java.sql.SQLException;

/**
 * Read and set a property value.
 */
public interface DtoReadSet {

  /**
   * Read the value from the dataReader and set it to the bean.
   */
  void readSet(Object bean, DataReader dataReader) throws SQLException;

  /**
   * Return true if this maps to a read only property (no setter method).
   */
  boolean isReadOnly();
}
