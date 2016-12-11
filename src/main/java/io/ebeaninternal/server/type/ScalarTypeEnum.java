package io.ebeaninternal.server.type;

import java.util.Set;

/**
 * Marker interface for the Enum scalar types.
 */
public interface ScalarTypeEnum {

  /**
   * Return the IN values for DB constraint construction.
   */
  Set<String> getDbCheckConstraintValues();

}
