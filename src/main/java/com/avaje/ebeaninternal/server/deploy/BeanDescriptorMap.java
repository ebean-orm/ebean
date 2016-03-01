package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeanservice.docstore.api.DocStoreBeanAdapter;

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
  ServerCacheManager getCacheManager();

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
  <T> DocStoreBeanAdapter<T> createDocStoreBeanAdapter(BeanDescriptor descriptor, DeployBeanDescriptor<T> deploy);
}
