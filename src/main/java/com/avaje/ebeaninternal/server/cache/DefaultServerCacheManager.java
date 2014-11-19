package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.cache.ServerCacheOptions;
import com.avaje.ebeaninternal.api.SpiEbeanServer;


/**
 * Manages the bean and query caches. 
 */
public class DefaultServerCacheManager implements ServerCacheManager {

	private final DefaultCacheHolder beanCache;

	private final DefaultCacheHolder queryCache;

	private final DefaultCacheHolder naturalKeyCache;

	private final DefaultCacheHolder collectionIdsCache;

	private final ServerCacheFactory cacheFactory;
	
	private SpiEbeanServer ebeanServer;
	
	/**
	 * Create with a cache factory and default cache options.
	 */
	public DefaultServerCacheManager(ServerCacheFactory cacheFactory, ServerCacheOptions defaultBeanOptions, ServerCacheOptions defaultQueryOptions) {
		this.cacheFactory = cacheFactory;
		this.beanCache = new DefaultCacheHolder(cacheFactory, defaultBeanOptions, true);
		this.queryCache = new DefaultCacheHolder(cacheFactory, defaultQueryOptions, false);
		this.naturalKeyCache = new DefaultCacheHolder(cacheFactory, defaultQueryOptions, false);
		this.collectionIdsCache = new DefaultCacheHolder(cacheFactory, defaultQueryOptions, false);
	}	
			
	public void init(EbeanServer server) {
		cacheFactory.init(server);
		this.ebeanServer = (SpiEbeanServer)server;
	}

  /**
   * Set bean caching on or off for a given bean type.
   */
  public void setCaching(Class<?> beanType, boolean useCache) {
    ebeanServer.getBeanDescriptor(beanType).setUseCache(useCache);
  }

	/**
	 * Clear both the bean cache and the query cache for a 
	 * given bean type.
	 */
	public void clear(Class<?> beanType) {
		String beanName = beanType.getName();
		beanCache.clearCache(beanName);
		naturalKeyCache.clearCache(beanName);
		collectionIdsCache.clearCache(beanName);
		queryCache.clearCache(beanName);
	}


	public void clearAll() {
		beanCache.clearAll();
		queryCache.clearAll();
		naturalKeyCache.clearAll();
		collectionIdsCache.clearAll();
	}

	
    public ServerCache getCollectionIdsCache(Class<?> beanType, String propertyName) {
	    return collectionIdsCache.getCache(beanType.getName()+"."+propertyName);
    }

	public boolean isCollectionIdsCaching(Class<?> beanType) {
		return collectionIdsCache.isCaching(beanType.getName());
	}
	
	public ServerCache getNaturalKeyCache(Class<?> beanType) {
	    return naturalKeyCache.getCache(beanType.getName());
    }
	
	public boolean isNaturalKeyCaching(Class<?> beanType) {
		return naturalKeyCache.isCaching(beanType.getName());
	}
	
	/**
	 * Return the query cache for a given bean type.
	 */
	public ServerCache getQueryCache(Class<?> beanType) {
		return queryCache.getCache(beanType.getName());
	}
	
	/**
	 * Return the bean cache for a given bean type.
	 */
	public ServerCache getBeanCache(Class<?> beanType) {
		return beanCache.getCache(beanType.getName());
	}

	/**
	 * Return true if there is an active cache for the given bean type.
	 */
	public boolean isBeanCaching(Class<?> beanType) {
		return beanCache.isCaching(beanType.getName());
	}


	public boolean isQueryCaching(Class<?> beanType) {
		return queryCache.isCaching(beanType.getName());
	}
	

}
