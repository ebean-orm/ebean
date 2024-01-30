package io.ebeaninternal.api;

import io.ebean.bean.FrozenBeans;
import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;

import java.util.List;
import java.util.Map;

/**
 * SPI extension to PersistenceContext.
 */
public interface SpiPersistenceContext extends PersistenceContext {

  /**
   * Return the list of dirty beans held by this persistence context.
   */
  List<Object> dirtyBeans(SpiBeanTypeManager manager);

  /**
   * Return all the entities from this persistence context.
   */
  Map<Class<?>, List<EntityBean>> detach();

  /**
   * Attach all the cached entities into this persistence context.
   */
  void attach(FrozenBeans cachedBeans);

}
