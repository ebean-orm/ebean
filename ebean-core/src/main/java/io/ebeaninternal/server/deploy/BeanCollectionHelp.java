package io.ebeaninternal.server.deploy;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionAdd;
import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.query.CQueryCollectionAdd;

import java.io.IOException;
import java.util.Collection;

/**
 * Helper functions for performing tasks on Lists Sets or Maps.
 */
public interface BeanCollectionHelp<T> extends CQueryCollectionAdd<T> {

  /**
   * Set the EbeanServer that owns the configuration.
   */
  void setLoader(BeanCollectionLoader loader);

  /**
   * Return the underlying collection of beans.
   */
  Collection underlying(Object value);

  /**
   * Return the mechanism to add beans to the underlying collection.
   * <p>
   * For Map's this needs to take the mapKey.
   * </p>
   */
  BeanCollectionAdd collectionAdd(Object bc, String mapKey);

  /**
   * Create an empty collection of the correct type without a parent bean.
   */
  @Override
  BeanCollection<T> createEmptyNoParent();

  /**
   * Create an empty collection of the correct type.
   */
  BeanCollection<T> createEmpty(EntityBean bean);

  /**
   * Create and return an empty 'vanilla' collection that does not support lazy loading.
   */
  Object createEmptyReference();

  /**
   * Add a bean to the List Set or Map.
   */
  @Override
  void add(BeanCollection<?> collection, EntityBean bean, boolean withCheck);

  /**
   * Create a lazy loading proxy for a List Set or Map.
   */
  BeanCollection<T> createReference(EntityBean parentBean);

  /**
   * Apply the new refreshed BeanCollection to the appropriate property of the parent bean.
   */
  void refresh(BeanCollection<?> bc, EntityBean parentBean);

  /**
   * Write the collection out as json.
   */
  void jsonWrite(SpiJsonWriter ctx, String name, Object collection, boolean explicitInclude) throws IOException;

}
