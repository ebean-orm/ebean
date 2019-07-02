package io.ebean.plugin;

import io.ebean.bean.EntityBean;

/**
 * Property of a entity bean that is a ToMany property.
 */
public interface PropertyAssocMany extends Property {

  /**
   * Add the loaded current bean to its associated parent.
   * <p>
   * Helper method used by Ebean Elastic integration when loading with a persistence context.
   */
  void lazyLoadMany(EntityBean current);
}
