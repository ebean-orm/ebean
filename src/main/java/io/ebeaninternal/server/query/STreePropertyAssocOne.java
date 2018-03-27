package io.ebeaninternal.server.query;

import io.ebeaninternal.server.type.ScalarType;

public interface STreePropertyAssocOne extends STreePropertyAssoc {

  /**
   * Return true if the property is an Id.
   */
  boolean isAssocId();

  /**
   * Return the scalar type of the associated id property.
   */
  ScalarType<?> getIdScalarType();
}
