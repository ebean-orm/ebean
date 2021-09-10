package io.ebeaninternal.server.query;

import io.ebean.bean.EntityBean;
import io.ebean.core.type.ScalarDataReader;

public interface STreePropertyAssocOne extends STreePropertyAssoc {

  /**
   * Return true if the property is an Id.
   */
  boolean isAssocId();

  /**
   * Return the scalar type of the associated id property.
   */
  ScalarDataReader<?> idReader();

  /**
   * Returns true, if this relation has a foreign key.
   */
  boolean hasForeignKey();

  /**
   * Return the property value as an entity bean from the parent.
   */
  EntityBean getValueAsEntityBean(EntityBean parentBean);
}
