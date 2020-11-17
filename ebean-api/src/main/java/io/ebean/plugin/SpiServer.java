package io.ebean.plugin;

import io.ebean.Database;
import io.ebean.bean.BeanLoader;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DatabasePlatform;

import java.util.List;

/**
 * Extensions to Database API made available to plugins.
 */
public interface SpiServer extends Database {

  /**
   * Return the DatabaseConfig.
   */
  DatabaseConfig getServerConfig();

  /**
   * Return the DatabasePlatform for this database.
   */
  DatabasePlatform getDatabasePlatform();

  /**
   * Return all the bean types registered on this server instance.
   */
  List<? extends BeanType<?>> getBeanTypes();

  /**
   * Return the bean type for a given entity bean class.
   */
  <T> BeanType<T> getBeanType(Class<T> beanClass);

  /**
   * Return the bean types mapped to the given base table.
   */
  List<? extends BeanType<?>> getBeanTypes(String baseTableName);

  /**
   * Return the bean type for a given doc store queueId.
   */
  BeanType<?> getBeanTypeForQueueId(String queueId);

  /**
   * Return a BeanLoader.
   */
  BeanLoader beanLoader();

  /**
   * Invoke lazy loading on this single bean (reference bean).
   */
  void loadBeanRef(EntityBeanIntercept ebi);

  /**
   * Invoke lazy loading on this single bean (L2 cache bean).
   */
  void loadBeanL2(EntityBeanIntercept ebi);

  /**
   * Invoke lazy loading on this single bean when no BeanLoader is set.
   * Typically due to serialisation or multiple stateless updates.
   */
  void loadBean(EntityBeanIntercept ebi);
}
