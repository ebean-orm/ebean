package com.avaje.ebeaninternal.server.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheOptions;
import com.avaje.ebean.cache.ServerCacheStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The default cache implementation.
 * <p>
 * It is base on ConcurrentHashMap with periodic trimming using a TimerTask.
 * The periodic trimming means that an LRU list does not have to be maintained.
 * </p>
 */
public class DefaultServerCache implements ServerCache {

	private static final Logger logger = LoggerFactory.getLogger(DefaultServerCache.class);

	private static final CacheEntryComparator comparator = new CacheEntryComparator();
		
	private final ConcurrentHashMap<Object, CacheEntry> map = new ConcurrentHashMap<Object, CacheEntry>();

	private final AtomicInteger missCount = new AtomicInteger();
	
	private final AtomicInteger removedHitCount = new AtomicInteger();
	
	private final Object monitor = new Object();

	private final String name;

	private int maxSize;

	private long trimFrequency;

	private int maxIdleSecs;

	private int maxSecsToLive;

	public DefaultServerCache(String name, ServerCacheOptions options) {
		this(name, options.getMaxSize(), options.getMaxIdleSecs(), options.getMaxSecsToLive());
	}

	public DefaultServerCache(String name, int maxSize, int maxIdleSecs, int maxSecsToLive) {
		this.name = name;
		this.maxSize = maxSize;
		this.maxIdleSecs = maxIdleSecs;
		this.maxSecsToLive = maxSecsToLive;
		this.trimFrequency = 60;

	}
	
	public void init(EbeanServer server) {
		
		TrimTask trim = new TrimTask();
		
		BackgroundExecutor executor = server.getBackgroundExecutor();
		executor.executePeriodically(trim, trimFrequency, TimeUnit.SECONDS);
	}
	
	
	
	
	public ServerCacheStatistics getStatistics(boolean reset) {

		ServerCacheStatistics s = new ServerCacheStatistics();
		s.setCacheName(name);
		s.setMaxSize(maxSize);

		// these counters won't necessarily be consistent with
		// respect to each other as activity can occur while
		// they are being calculated
		int mc = reset ? missCount.getAndSet(0) : missCount.get();
		int hc = getHitCount(reset);
		int size = size();
		
		s.setSize(size);
		s.setHitCount(hc);
		s.setMissCount(mc);
		
		return s;
	}
	
	public int getHitRatio() {

		int mc = missCount.get();
		int hc = getHitCount(false);
		
		int totalCount = hc + mc;
		if (totalCount == 0){
			return 0;
		} else {
			return hc * 100 / totalCount;
		}

	}
	
	private int getHitCount(boolean reset) {
		
		int hc = reset ? removedHitCount.getAndSet(0) : removedHitCount.get();
		
		for (CacheEntry cacheEntry : map.values()) {
			hc += cacheEntry.getHitCount(reset);
		}
		
		return hc;
	}


	public ServerCacheOptions getOptions() {
		synchronized (monitor) {
			ServerCacheOptions o = new ServerCacheOptions();
			o.setMaxIdleSecs(maxIdleSecs);
			o.setMaxSize(maxSize);
			o.setMaxSecsToLive(maxSecsToLive);
			return o;
		}
	}
	
	public void setOptions(ServerCacheOptions o) {
		synchronized (monitor) {
			maxIdleSecs = o.getMaxIdleSecs();
			maxSize = o.getMaxSize();
			maxSecsToLive = o.getMaxSecsToLive();
		}
	}
	
	
	/**
	 * Return the max cache size.
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * Set the max cache size.
	 */
	public void setMaxSize(int maxSize) {
		synchronized (monitor) {
			this.maxSize = maxSize;
		}
	}

	/**
	 * Return the max idle time.
	 */
	public long getMaxIdleSecs() {
		return maxIdleSecs;
	}

	/**
	 * Set the max idle time.
	 */
	public void setMaxIdleSecs(int maxIdleSecs) {
		synchronized (monitor) {
			this.maxIdleSecs = maxIdleSecs;
		}
	}

	/**
	 * Return the maximum time to live.
	 */
	public long getMaxSecsToLive() {
		return maxSecsToLive;
	}

	/**
	 * Set the maximum time to live.
	 */
	public void setMaxSecsToLive(int maxSecsToLive) {
		synchronized (monitor) {
			this.maxSecsToLive = maxSecsToLive;
		}
	}
	
	/**
	 * Return the name of the cache.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Clear the cache.
	 */
	public void clear() {
		map.clear();
	}

	/**
	 * Return a value from the cache.
	 */
	public Object get(Object key) {
		
		CacheEntry entry = map.get(key);
		
		if (entry == null){
			missCount.incrementAndGet();
			return null;
			
		} else {
			// get value incrementing last 
			// access time and hitCount
			return entry.getValue();
		}
	}

	/**
	 * Put a value into the cache.
	 */
	public Object put(Object key, Object value) {
		// put new entry with create time
		CacheEntry entry = map.put(key, new CacheEntry(key, value));
		if (entry == null){
			return null;
		} else {
			int removedHits = entry.getHitCount(true);
			removedHitCount.addAndGet(removedHits);
			return entry.getValue();
		}
	}

	/**
	 * Put a value into the cache but only if absent.
	 */
	public Object putIfAbsent(Object key, Object value) {
		CacheEntry entry = map.putIfAbsent(key, new CacheEntry(key, value));
		if (entry == null){
			return null;
		} else {
			return entry.getValue();
		}
	}

	/**
	 * Remove an entry from the cache.
	 */
	public Object remove(Object key) {
		CacheEntry entry = map.remove(key);
		if (entry == null){
			return null;
		} else {
			int removedHits = entry.getHitCount(true);
			removedHitCount.addAndGet(removedHits);
			return entry.getValue();
		}
	}

	/**
	 * Return the number of elements in the cache.
	 */
	public int size() {
		return map.size();
	}

	/**
	 * The task used to periodically trim the cache.
	 */
	private class TrimTask implements Runnable {

		public void run() {

			long startTime = System.currentTimeMillis();
			
			if (logger.isTraceEnabled()){
				logger.trace("trimming cache " + name);
			}
			
			int trimmedByIdle = 0;
			int trimmedByTTL = 0;
			int trimmedByLRU = 0;

			boolean trimMaxSize = maxSize > 0 && maxSize < size();

			ArrayList<CacheEntry> activeList = new ArrayList<CacheEntry>();

			long idleExpire = System.currentTimeMillis() - (maxIdleSecs*1000);
			long ttlExpire = System.currentTimeMillis() - (maxSecsToLive*1000);

			Iterator<CacheEntry> it = map.values().iterator();
			while (it.hasNext()) {
				CacheEntry cacheEntry = it.next();
				if (maxIdleSecs > 0 && idleExpire > cacheEntry.getLastAccessTime()) {
					it.remove();
					trimmedByIdle++;

				} else if (maxSecsToLive > 0 && ttlExpire > cacheEntry.getCreateTime()) {
					it.remove();
					trimmedByTTL++;

				} else if (trimMaxSize) {
					activeList.add(cacheEntry);
				}
			}

			if (trimMaxSize) {
				trimmedByLRU = activeList.size() - maxSize;

				if (trimmedByLRU > 0) {
					// sort into last access time ascending
					Collections.sort(activeList, comparator);
					for (int i = maxSize; i < activeList.size(); i++) {
						// remove if still in the cache
						map.remove(activeList.get(i).getKey());
					}
				}
			}
			
			long exeTime = System.currentTimeMillis() - startTime;
			
			if (logger.isDebugEnabled()){
				logger.debug("Executed trim of cache " + name + " in ["+exeTime
					+"]millis  idle[" + trimmedByIdle + "] timeToLive[" 
					+ trimmedByTTL + "] accessTime["
					+ trimmedByLRU + "]");
			}

		}

	}

	/**
	 * Comparator for sorting by last access time.
	 */
	private static class CacheEntryComparator implements Comparator<CacheEntry>, Serializable {

		private static final long serialVersionUID = 1L;

		public int compare(CacheEntry o1, CacheEntry o2) {
			
			return o1.getLastAccessLong().compareTo(o2.getLastAccessLong());
		}
	}
	
	/**
	 * Wraps the values to additionally hold createTime and lastAccessTime.
	 */
	public static class CacheEntry {

		private final Object key;
		private final Object value;
		private final long createTime;
		private final AtomicInteger hitCount = new AtomicInteger();
		private Long lastAccessTime;

		public CacheEntry(Object key, Object value) {
			this.key = key;
			this.value = value;
			this.createTime = System.currentTimeMillis();
			this.lastAccessTime = Long.valueOf(createTime);
		}

		public Object getKey() {
			return key;
		}

		public Object getValue() {
			// object assignment is atomic
			hitCount.incrementAndGet();
			this.lastAccessTime = Long.valueOf(System.currentTimeMillis());
			return value;
		}

		public long getCreateTime() {
			return createTime;
		}

		public long getLastAccessTime() {
			return lastAccessTime.longValue();
		}

		public Long getLastAccessLong() {
			return lastAccessTime;
		}

		public int getHitCount(boolean reset) {
			if (reset){
				return hitCount.getAndSet(0);

			} else {
				return hitCount.get();				
			}
		}
		public int getHitCount() {
			return hitCount.get();
		}
	}
}
