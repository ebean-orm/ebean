package com.avaje.ebeaninternal.server.autofetch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.PersistenceException;

import com.avaje.ebean.bean.CallStack;
import com.avaje.ebean.bean.NodeUsageCollector;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebean.config.AutofetchConfig;
import com.avaje.ebean.config.AutofetchMode;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.ClassUtil;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The manager of all the usage/query statistics as well as the tuned fetch
 * information.
 */
public class DefaultAutoFetchManager implements AutoFetchManager, Serializable {

  private static final Logger logger = LoggerFactory.getLogger(DefaultAutoFetchManager.class);

	private static final long serialVersionUID = -6826119882781771722L;

	private final String statisticsMonitor = new String();

	private final String fileName;

	/**
	 * Map of the usage and query statistics gathered.
	 */
	private Map<String, Statistics> statisticsMap = new ConcurrentHashMap<String, Statistics>();

	/**
	 * Map of the tuned query details per profile query point.
	 */
	private Map<String, TunedQueryInfo> tunedQueryInfoMap = new ConcurrentHashMap<String, TunedQueryInfo>();

	private transient long defaultGarbageCollectionWait = 100;

	/**
	 * Left without synchronized for now.
	 */
	private transient int tunedQueryCount;
	
	/**
	 * Converted from a 0-100 int to a double. Effectively a percentage rate at
	 * which to collect profiling information.
	 */
	private transient double profilingRate = 0.1d;

	private transient int profilingBase = 10;

	private transient int profilingMin = 1;

	private transient boolean profiling;

	private transient boolean queryTuning;

	private transient boolean queryTuningAddVersion;

	private transient boolean garbageCollectionOnShutdown;
	
	private transient AutofetchMode mode;
	
	/**
	 * Server that owns this Profile Listener.
	 */
	private transient SpiEbeanServer server;

	/**
	 * The logger.
	 */
	private transient DefaultAutoFetchManagerLogging logging;

	public DefaultAutoFetchManager(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Set up this profile listener before it is active.
	 */
	public void setOwner(SpiEbeanServer server, ServerConfig serverConfig) {
		this.server = server;
		this.logging = new DefaultAutoFetchManagerLogging(serverConfig, this);
		
		AutofetchConfig autofetchConfig = serverConfig.getAutofetchConfig();
		
		garbageCollectionOnShutdown = autofetchConfig.isGarbageCollectionOnShutdown();
		queryTuning = autofetchConfig.isQueryTuning();
		queryTuningAddVersion = autofetchConfig.isQueryTuningAddVersion();
		profiling = autofetchConfig.isProfiling();
		profilingMin = autofetchConfig.getProfilingMin();
		profilingBase = autofetchConfig.getProfilingBase();

		setProfilingRate(autofetchConfig.getProfilingRate());
				
		defaultGarbageCollectionWait = (long) autofetchConfig.getGarbageCollectionWait();

		// determine the mode to use when Query.setAutoFetch() was
		// not explicitly set
		mode = autofetchConfig.getMode();

		if (profiling || queryTuning) {
			// log the guts of the autoFetch setup
			String msg = "AutoFetch queryTuning[" + queryTuning + "] profiling[" + profiling
					+ "] mode[" + mode + "]  profiling rate[" + profilingRate
					+ "] min[" + profilingMin + "] base[" + profilingBase + "]";
			logging.logInfo(msg, null);

			// Register a periodic update of the profiling informations
	    this.logging.init(server);
		}
	}

	
	
	public void clearQueryStatistics() {
		server.clearQueryStatistics();
	}

	/**
	 * Return the number of queries tuned by AutoFetch.
	 */
	public int getTotalTunedQueryCount(){
		return tunedQueryCount;
	}
	
	/**
	 * Return the size of the TuneQuery map.
	 */
	public int getTotalTunedQuerySize(){
		return tunedQueryInfoMap.size();
	}
	
	/**
	 * Return the size of the profile map.
	 */
	public int getTotalProfileSize(){
		return statisticsMap.size();
	}
	
	public int clearTunedQueryInfo() {
		
		// reset the rough count as well
		tunedQueryCount = 0;
		
		// clear the map...
		int size = tunedQueryInfoMap.size();
		tunedQueryInfoMap.clear();
		return size;
	}

	public int clearProfilingInfo() {
		int size = statisticsMap.size();
		statisticsMap.clear();
		return size;
	}

	
	public void serialize() {

		File autoFetchFile = new File(fileName);

		try {
			FileOutputStream fout = new FileOutputStream(autoFetchFile);

			ObjectOutputStream oout = new ObjectOutputStream(fout);
			oout.writeObject(this);
			oout.flush();
			oout.close();

		} catch (Exception e) {
			String msg = "Error serializing autofetch file";
			logging.logError(msg, e);
		}
	}

	/**
	 * Return the current Tuned query info for a given origin key.
	 */
	public TunedQueryInfo getTunedQueryInfo(String originKey) {
		return tunedQueryInfoMap.get(originKey);
	}

	/**
	 * Return the current Statistics for a given originKey key.
	 */
	public Statistics getStatistics(String originKey) {
		return statisticsMap.get(originKey);
	}

	public Iterator<TunedQueryInfo> iterateTunedQueryInfo() {
		return tunedQueryInfoMap.values().iterator();
	}

	public Iterator<Statistics> iterateStatistics() {
		return statisticsMap.values().iterator();
	}

	public boolean isProfiling() {
		return profiling;
	}

	/**
	 * When the application is running, BEFORE turning off profiling you
	 * probably should call collectUsageViaGC() as there is a delay (waiting for
	 * garbage collection) collecting usage profiling information.
	 */
	public void setProfiling(boolean profiling) {
		this.profiling = profiling;
	}

	public boolean isQueryTuning() {
		return queryTuning;
	}

	public void setQueryTuning(boolean queryTuning) {
		this.queryTuning = queryTuning;
	}
	
	public double getProfilingRate() {
		return profilingRate;
	}

	public AutofetchMode getMode() {
		return mode;
	}

	public void setMode(AutofetchMode mode) {
		this.mode = mode;
	}

	public void setProfilingRate(double rate) {
		if (rate < 0) {
			rate = 0d;
		} else if (rate > 1) {
			rate = 1d;
		}
		profilingRate = rate;
	}

	public int getProfilingBase() {
		return profilingBase;
	}

	public void setProfilingBase(int profilingBase) {
		this.profilingBase = profilingBase;
	}

	public int getProfilingMin() {
		return profilingMin;
	}

	public void setProfilingMin(int profilingMin) {
		this.profilingMin = profilingMin;
	}

	/**
	 * Shutdown the listener.
	 * <p>
	 * We should try to collect the usage statistics by calling a System.gc().
	 * This is necessary for use with short lived applications where garbage
	 * collection may not otherwise occur at all.
	 * </p>
	 */
	public void shutdown() {
		if (garbageCollectionOnShutdown) {
		    collectUsageViaGC(-1);
		    serialize();
		}
	}

	/**
	 * Ask for a System.gc() so that we gather node usage information.
	 * <p>
	 * Really only want to do this sparingly but useful just prior to shutdown
	 * for short run application where garbage collection may otherwise not
	 * occur at all.
	 * </p>
	 * <p>
	 * waitMillis will do a thread sleep to give the garbage collection a little
	 * time to do its thing assuming we are shutting down the VM.
	 * </p>
	 * <p>
	 * If waitMillis is -1 then the defaultGarbageCollectionWait is used which
	 * defaults to 100 milliseconds.
	 * </p>
	 */
	public String collectUsageViaGC(long waitMillis) {
		System.gc();
		try {
			if (waitMillis < 0) {
				waitMillis = defaultGarbageCollectionWait;
			}
			Thread.sleep(waitMillis);
		} catch (InterruptedException e) {
			String msg = "Error while sleeping after System.gc() request.";
			logging.logError(msg, e);
			return msg;
		}
		return updateTunedQueryInfo();
	}

	/**
	 * Update the tuned fetch plans from the current usage information.
	 */
	public String updateTunedQueryInfo() {

		if (!profiling) {
			// we are not collecting any profiling information at
			// the moment so don't try updating the tuned query plans.
			return "Not profiling";
		}

		synchronized (statisticsMonitor) {

		    Counters counters = new Counters();
		    
			Iterator<Statistics> it = statisticsMap.values().iterator();
			while (it.hasNext()) {
				Statistics queryPointStatistics = it.next();
				if (!queryPointStatistics.hasUsage()){
				    // no usage statistics collected yet...
				    counters.incrementNoUsage();
				} else {
				    updateTunedQueryFromUsage(counters, queryPointStatistics);
				}
			}

			String summaryInfo = counters.toString();

			if (counters.isInteresting()){
				// only log it if its interesting
				logging.logSummary(summaryInfo);
			}
			
			return summaryInfo;
		}
	}

	private static class Counters {
	    
        int newPlan;
        int modified;
        int unchanged;
        int noUsage;
        
        void incrementNoUsage(){
            noUsage++;
        }
        void incrementNew(){
            newPlan++;
        }
        void incrementModified(){
            modified++;
        }
        void incrementUnchanged(){
            unchanged++;
        }
        boolean isInteresting() {
            return newPlan > 0 || modified > 0;
        }
        public String toString() {
            return "new["+newPlan+"] modified["+modified+"] unchanged["+unchanged+"] nousage["+noUsage+"]";
        }
	}
	
	private void updateTunedQueryFromUsage(Counters counters, Statistics statistics) {
	    
	    ObjectGraphOrigin queryPoint = statistics.getOrigin();
        String beanType = queryPoint.getBeanType();

        try {
            Class<?> beanClass = ClassUtil.forName(beanType, this.getClass());
            BeanDescriptor<?> beanDescriptor = server.getBeanDescriptor(beanClass);
            if (beanDescriptor == null){
                // previously was an entity but not longer
                
            } else {
                // Determine the fetch plan from the latest statistics.
                // Use this to compare with current "tuned fetch plan".
                OrmQueryDetail newFetchDetail = statistics.buildTunedFetch(beanDescriptor);
                
                // get the current tuned fetch info...
                TunedQueryInfo currentFetch = tunedQueryInfoMap.get(queryPoint.getKey());

                if (currentFetch == null) {
                    // its a new fetch plan, add it.
                    counters.incrementNew();

                    currentFetch = statistics.createTunedFetch(newFetchDetail);
                    logging.logNew(currentFetch);
                    tunedQueryInfoMap.put(queryPoint.getKey(), currentFetch);

                } else if (!currentFetch.isSame(newFetchDetail)) {
                    // the fetch plan has changed, update it.
                    counters.incrementModified();
                    
                    logging.logChanged(currentFetch, newFetchDetail);
                    currentFetch.setTunedDetail(newFetchDetail);

                } else {
                    // the fetch plan has not changed...
                    counters.incrementUnchanged();
                }

                currentFetch.setProfileCount(statistics.getCounter());
            }

        } catch (ClassNotFoundException e) {
            // expected after renaming/moving an entity bean
            String msg = e.toString()+" updating autoFetch tuned query for " + beanType
                +". It isLikely this bean has been renamed or moved";
            logging.logInfo(msg, null);
            statisticsMap.remove(statistics.getOrigin().getKey());
        }
	}
	
	/**
	 * Return true if we should try to use autoFetch for this query.
	 */
	private boolean useAutoFetch(SpiQuery<?> query) {

		if (query.isLoadBeanCache()){
			// when loading the cache don't tune the query
			// as we want full objects loaded into the cache
			return false;
		}
		
		Boolean autoFetch = query.isAutofetch();
		if (autoFetch != null) {
			// explicitly set...
			return autoFetch.booleanValue();

		} else {
			// determine using implicit mode...
			switch (mode) {
			case DEFAULT_ON:
				return true;

			case DEFAULT_OFF:
				return false;

			case DEFAULT_ONIFEMPTY:
				return query.isDetailEmpty();

			default:
				throw new PersistenceException("Invalid autoFetchMode " + mode);
			}
		}
	}

	/**
	 * Auto tune the query and enable profiling.
	 */
	public boolean tuneQuery(SpiQuery<?> query) {

		if (!queryTuning && !profiling) {
			return false;
		}

		if (!useAutoFetch(query)) {
			// not using autoFetch for this query
			return false;
		}

		ObjectGraphNode parentAutoFetchNode = query.getParentNode();
		if (parentAutoFetchNode != null) {
			// This is a +lazy/+query query with profiling on.
			// We continue to collect the profiling information.
			query.setAutoFetchManager(this);
			return true;
		}

		// create a query point to identify the query
		CallStack stack = server.createCallStack();
		ObjectGraphNode origin = query.setOrigin(stack);

		// get current "tuned fetch" for this query point
		TunedQueryInfo tunedFetch = tunedQueryInfoMap.get(origin.getOriginQueryPoint().getKey());

		// get the number of times we have collected profiling information
		int profileCount = tunedFetch == null ? 0 : tunedFetch.getProfileCount();

		if (profiling) {
			// we want more profiling information?
			if (tunedFetch == null) {
				query.setAutoFetchManager(this);

			} else if (profileCount < profilingBase) {
				query.setAutoFetchManager(this);

			} else if (tunedFetch.isPercentageProfile(profilingRate)) {
				query.setAutoFetchManager(this);
			}
		}
		
		if (queryTuning) {
			if (tunedFetch != null && profileCount >= profilingMin) {
				// deemed to have enough profiling 
				// information for automatic tuning
				if (tunedFetch.autoFetchTune(query)){
					// tunedQueryCount++ not thread-safe, could use AtomicInteger.
					// But I'm happy if this statistic is a little wrong
					// and this is a VERY HOT method
					tunedQueryCount++;
				}	
				return true;
			}
		}

		return false;
	}

	/**
	 * Gather query execution statistics. This could either be the originating
	 * query in which case the parentNode will be null, or a lazy loading query
	 * resulting from traversal of the object graph.
	 */
	public void collectQueryInfo(ObjectGraphNode node, long beans, long micros) {

		if (node != null){
			ObjectGraphOrigin origin = node.getOriginQueryPoint();
			if (origin != null){
				Statistics stats = getQueryPointStats(origin);
				stats.collectQueryInfo(node, beans, micros);				
			}
		}
	}

	/**
	 * Collect usage statistics from a node in the object graph.
	 * <p>
	 * This is sent to use from a EntityBeanIntercept when the finalise method
	 * is called on the bean.
	 * </p>
	 */
	public void collectNodeUsage(NodeUsageCollector usageCollector) {

		ObjectGraphOrigin origin = usageCollector.getNode().getOriginQueryPoint();

		Statistics stats = getQueryPointStats(origin);

    if (logger.isTraceEnabled()) {
      logger.trace("... NodeUsageCollector " + usageCollector);
    }

    stats.collectUsageInfo(usageCollector);

    if (logger.isTraceEnabled()) {
      logger.trace("stats\n" + stats);
    }
  }

	private Statistics getQueryPointStats(ObjectGraphOrigin originQueryPoint) {
		synchronized (statisticsMonitor) {
			Statistics stats = statisticsMap.get(originQueryPoint.getKey());
			if (stats == null) {
				stats = new Statistics(originQueryPoint, queryTuningAddVersion);
				statisticsMap.put(originQueryPoint.getKey(), stats);
			}
			return stats;
		}
	}

	public String toString() {
		synchronized (statisticsMonitor) {
			return statisticsMap.values().toString();
		}
	}


}
