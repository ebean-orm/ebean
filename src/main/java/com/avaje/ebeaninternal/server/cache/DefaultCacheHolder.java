package com.avaje.ebeaninternal.server.cache;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.avaje.ebean.annotation.CacheTuning;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheOptions;

/**
 * Manages the construction of caches.
 */
public class DefaultCacheHolder {

	private final ConcurrentHashMap<String, ServerCache> concMap = new ConcurrentHashMap<String, ServerCache>();

	private final HashMap<String, ServerCache> synchMap = new HashMap<String, ServerCache>();

	private final Object monitor = new Object();

	private final ServerCacheFactory cacheFactory;

	private final ServerCacheOptions defaultOptions;

	private final boolean useBeanTuning;

	/**
	 * Create with a cache factory and default cache options.
	 * 
	 * @param cacheFactory
	 *            the factory for creating the cache
	 * @param defaultOptions
	 *            the default options for tuning the cache
	 * @param useBeanTuning
	 *            if true then use the bean class specific tuning. This is
	 *            generally false for the query cache.
	 */
	public DefaultCacheHolder(ServerCacheFactory cacheFactory,
			ServerCacheOptions defaultOptions, boolean useBeanTuning) {

		this.cacheFactory = cacheFactory;
		this.defaultOptions = defaultOptions;
		this.useBeanTuning = useBeanTuning;
	}

	/**
	 * Return the default cache options.
	 */
	public ServerCacheOptions getDefaultOptions() {
		return defaultOptions;
	}

	/**
	 * Return the cache for a given bean type.
	 */
	public ServerCache getCache(String cacheKey) {

		ServerCache cache = concMap.get(cacheKey);
		if (cache != null) {
			return cache;
		}
		synchronized (monitor) {
			cache = synchMap.get(cacheKey);
			if (cache == null) {
				ServerCacheOptions options = getCacheOptions(cacheKey);
				cache = cacheFactory.createCache(cacheKey, options);
				synchMap.put(cacheKey, cache);
				concMap.put(cacheKey, cache);
			}
			return cache;
		}
	}

	public void clearCache(String cacheKey) {

		ServerCache cache = concMap.get(cacheKey);
		if (cache != null) {
			cache.clear();
		}
	}
	
	/**
	 * Return true if there is an active cache for this bean type.
	 */
	public boolean isCaching(String beanType) {
		return concMap.containsKey(beanType);
	}

	public void clearAll() {
		for (ServerCache serverCache : concMap.values()) {
			serverCache.clear();
		}
	}

	/**
	 * Return the cache options for a given bean type.
	 */
	private ServerCacheOptions getCacheOptions(String beanType) {

		if (useBeanTuning) {
			// read the deployment annotation
			try {
				Class<?> cls = Class.forName(beanType);
				CacheTuning cacheTuning = cls.getAnnotation(CacheTuning.class);
				if (cacheTuning != null) {
					ServerCacheOptions o = new ServerCacheOptions(cacheTuning);
					o.applyDefaults(defaultOptions);
					return o;
				}
			} catch (ClassNotFoundException e){
				// ignore
			}
		}

		return defaultOptions.copy();

	}

}
