package com.avaje.ebeaninternal.server.core;

/**
 * Options for controlling cache behaviour for a given type.
 */
public class CacheOptions {
	
	private boolean useCache;

	private boolean readOnly;
	
	private String naturalKey;
	
	private String warmingQuery;
	
	/**
	 * Construct with options.
	 */
	public CacheOptions() {
	}
	
	/**
	 * Return true if this should use a cache for lazy loading.
	 */
	public boolean isUseCache() {
		return useCache;
	}
	
	/**
	 * Set whether to use the bean cache for the associated type.
	 */
	public void setUseCache(boolean useCache) {
    	this.useCache = useCache;
    }

	/**
	 * Return the readOnly default setting.
	 */
	public boolean isReadOnly() {
    	return readOnly;
    }

	/**
	 * Set read Only default setting.
	 */
	public void setReadOnly(boolean readOnly) {
    	this.readOnly = readOnly;
    }

	/**
	 * Return the query used to warm the cache.
	 */
	public String getWarmingQuery() {
		return warmingQuery;
	}

	/**
	 * Set the cache warming query.
	 */
	public void setWarmingQuery(String warmingQuery) {
    	this.warmingQuery = warmingQuery;
    }	


	/**
	 * Return true if a natural key is set.
	 */
	public boolean isUseNaturalKeyCache() {
		return naturalKey != null;
	}
	
	/**
	 * Return the natural key property.
	 */
	public String getNaturalKey() {
    	return naturalKey;
    }

	/**
	 * Set the natural key property.
	 */
	public void setNaturalKey(String naturalKey) {
		if (naturalKey == null || naturalKey.length() == 0){
			naturalKey = null;
		} else {
			this.naturalKey = naturalKey.trim();
		}
    }
	
}
