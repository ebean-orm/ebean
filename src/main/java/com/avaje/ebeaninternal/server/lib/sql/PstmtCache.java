package com.avaje.ebeaninternal.server.lib.sql;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A LRU based cache for PreparedStatements.
 */
public class PstmtCache extends LinkedHashMap<String, ExtendedPreparedStatement> {

	private static final Logger logger = Logger.getLogger(PstmtCache.class.getName());
	
    static final long serialVersionUID = -3096406924865550697L;

	/**
	 * The name of the cache, for tracing purposes.
	 */
	final String cacheName;
	
	/**
	 * The maximum size of the cache.  When this is exceeded the oldest entry is removed.
	 */
	final int maxSize;

	/**
	 * The total number of entries removed from this cache.
	 */
	int removeCounter;

	/**
	 * The number of get hits.
	 */
	int hitCounter;

	/** 
	 * The number of get() misses.
	 */
	int missCounter;

	/**
	 * The number of puts into this cache.
	 */
	int putCounter;

	public PstmtCache(String cacheName, int maxCacheSize) {

		// note = access ordered list.  This is what gives it the LRU order
		super(maxCacheSize*3, 0.75f, true);
		this.cacheName = cacheName;
		this.maxSize = maxCacheSize;
	}

	/**
	 * Return a summary description of this cache.
	 */
	public String getDescription() {
		return cacheName+" size:"+size()+" max:"+maxSize+" totalHits:"+hitCounter+" hitRatio:"+getHitRatio()+" removes:"+removeCounter;
	}
	
	/**
	 * returns the current maximum size of the cache.
	 */
	public int getMaxSize() {
		return maxSize;
	}		

	/**
	 * Gets the hit ratio.  A number between 0 and 100 indicating the number of
	 * hits to misses.  A number approaching 100 is desirable.
	 */
	public int getHitRatio() {
		if (hitCounter == 0) {
			return 0;
		} else {
			return hitCounter*100/(hitCounter+missCounter);
		}
	}

	/**
	 * The total number of hits against this cache.
	 */
	public int getHitCounter() {
		return hitCounter;
	}

	/**
	 * The total number of misses against this cache.
	 */
	public int getMissCounter() {
		return missCounter;
	}

	/**
	 * The total number of puts against this cache.
	 */
	public int getPutCounter() {
		return putCounter;
	}

	/**
	 * additionally maintains hit and miss statistics.
	 */
	public ExtendedPreparedStatement get(Object key) {

		ExtendedPreparedStatement o = super.get(key);
		if (o == null) {
			missCounter++;
		} else {
			hitCounter++;
		}
		return o;
	}

	/**
	 * additionally maintains hit and miss statistics.
	 */
	public ExtendedPreparedStatement remove(Object key) {

		ExtendedPreparedStatement o = super.remove(key);
		if (o == null) {
			missCounter++;
		} else {
			hitCounter++;
		}
		return o;
	}

	/**
	 * additionally maintains put counter statistics.
	 */
	public ExtendedPreparedStatement put(String key, ExtendedPreparedStatement value) {

		putCounter++;
		return super.put(key, value);
	}

	/**
	 * will check to see if we need to remove entries and
	 * if so call the cacheCleanup.cleanupEldestLRUCacheEntry() if
	 * one has been set.
	 */
	protected boolean removeEldestEntry(Map.Entry<String,ExtendedPreparedStatement> eldest) {
		
		if (size() < maxSize) {
			return false;
		}
		
		removeCounter++;
		
		try {
			ExtendedPreparedStatement pstmt = eldest.getValue();
			pstmt.closeDestroy();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Error closing ExtendedPreparedStatement", e);
		}
		return true;		
	}
	

}
 
