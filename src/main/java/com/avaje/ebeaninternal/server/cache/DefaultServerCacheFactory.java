package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCacheOptions;


/**
 * Default implementation of ServerCacheFactory.
 */
public class DefaultServerCacheFactory implements ServerCacheFactory {

	private EbeanServer ebeanServer;
	
	public void init(EbeanServer ebeanServer){
		this.ebeanServer = ebeanServer;
	}
	
	public ServerCache createCache(String cacheKey, ServerCacheOptions cacheOptions) {
		
		ServerCache cache =  new DefaultServerCache(cacheKey, cacheOptions);	
		cache.init(ebeanServer);
		return cache;
	}
	
}
