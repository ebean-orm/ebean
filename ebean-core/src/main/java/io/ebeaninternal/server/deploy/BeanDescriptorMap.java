package io.ebeaninternal.server.deploy;

import io.ebean.config.DatabaseConfig;
import io.ebean.config.EncryptKey;
import io.ebean.config.NamingConvention;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

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
  String name();

  /**
   * Return the DatabaseConfig.
   */
  DatabaseConfig config();

  /**
   * Return the Cache Manager.
   */
  SpiCacheManager cacheManager();

  /**
   * Return the naming convention.
   */
  NamingConvention namingConvention();

  /**
   * Return true if multiple values can be bound as Array or Table Value and hence share the same query plan.
   */
  boolean isMultiValueSupported();

  /**
   * Return the BeanDescriptor for a given class.
   */
  <T> BeanDescriptor<T> descriptor(Class<T> entityType);

  /**
   * Return the Encrypt key given the table and column name.
   */
  EncryptKey encryptKey(String tableName, String columnName);

  /**
   * Create a IdBinder for this bean property.
   */
  IdBinder createIdBinder(BeanProperty id);

  /**
   * Return the scalarType for the given JDBC type.
   */
  ScalarType<?> scalarType(int jdbcType);

  /**
   * Return the scalarType for the given logical type.
   */
  ScalarType<?> scalarType(String cast);

  /**
   * Return true if Jackson core is present on the classpath.
   */
  boolean isJacksonCorePresent();

  /**
   * Returns true, if the given table (or view) is managed by ebean
   * (= an entity exists)
   */
  boolean isTableManaged(String tableName);
}
