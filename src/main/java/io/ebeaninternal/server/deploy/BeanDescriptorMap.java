package io.ebeaninternal.server.deploy;

import io.ebean.config.EncryptKey;
import io.ebean.config.NamingConvention;
import io.ebean.config.ServerConfig;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.type.ScalarType;
import io.ebeanservice.docstore.api.DocStoreBeanAdapter;

/**
 * Provides a method to find a BeanDescriptor.
 * <p>
 * Used during deployment of to resolve relationships between beans.
 * </p>
 */
public interface BeanDescriptorMap {

  /**
   * Return the name of the server/database.
   */
  String getServerName();

  /**
   * Return the ServerConfig.
   */
  ServerConfig getServerConfig();

  /**
   * Return the Cache Manager.
   */
  SpiCacheManager getCacheManager();

  /**
   * Return the naming convention.
   */
  NamingConvention getNamingConvention();

  /**
   * Return true if multiple values can be bound as Array or Table Value and hence share the same query plan.
   */
  boolean isMultiValueSupported();

  /**
   * Return the BeanDescriptor for a given class.
   */
  <T> BeanDescriptor<T> getBeanDescriptor(Class<T> entityType);

  /**
   * Return the Encrypt key given the table and column name.
   */
  EncryptKey getEncryptKey(String tableName, String columnName);

  /**
   * Create a IdBinder for this bean property.
   */
  IdBinder createIdBinder(BeanProperty id);

  /**
   * Create a doc store specific adapter for this bean type.
   */
  <T> DocStoreBeanAdapter<T> createDocStoreBeanAdapter(BeanDescriptor<T> descriptor, DeployBeanDescriptor<T> deploy);

  /**
   * Return the scalarType for the given JDBC type.
   */
  ScalarType<?> getScalarType(int jdbcType);

  /**
   * Return the scalarType for the given logical type.
   */
  ScalarType<?> getScalarType(String cast);
}
