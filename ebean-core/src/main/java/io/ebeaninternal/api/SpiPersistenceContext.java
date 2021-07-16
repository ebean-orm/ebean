package io.ebeaninternal.api;

import io.ebean.bean.PersistenceContext;

import java.util.List;

/**
 * SPI extension to PersistenceContext.
 */
public interface SpiPersistenceContext extends PersistenceContext {

  /**
   * Return the list of dirty beans held by this persistence context.
   */
  List<Object> dirtyBeans(SpiBeanTypeManager manager);

}
