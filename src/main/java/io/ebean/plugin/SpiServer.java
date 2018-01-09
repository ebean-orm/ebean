package io.ebean.plugin;

import io.ebean.EbeanServer;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;

import javax.sql.DataSource;
import java.util.List;

/**
 * Extensions to EbeanServer API made available to plugins.
 */
public interface SpiServer extends EbeanServer {

  /**
   * Return the serverConfig.
   */
  ServerConfig getServerConfig();

  /**
   * Return the DatabasePlatform for this server.
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
   * Return the associated DataSource for this EbeanServer instance.
   */
  DataSource getDataSource();

  /**
   * Return the associated read only DataSource for this EbeanServer instance (can be null).
   */
  DataSource getReadOnlyDataSource();

}
