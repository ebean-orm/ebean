package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;

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
    public String getServerName();

    /**
     * Return the Cache Manager.
     */
    public ServerCacheManager getCacheManager();

    /**
     * Return the BeanDescriptor for a given class.
     */
    public <T> BeanDescriptor<T> getBeanDescriptor(Class<T> entityType);

    /**
     * Return the Encrypt key given the table and column name.
     */
    public EncryptKey getEncryptKey(String tableName, String columnName);
    
    public IdBinder createIdBinder(BeanProperty id);

}
