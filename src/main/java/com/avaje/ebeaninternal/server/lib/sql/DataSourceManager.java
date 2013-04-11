package com.avaje.ebeaninternal.server.lib.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.api.ClassUtil;
import com.avaje.ebeaninternal.server.lib.BackgroundRunnable;
import com.avaje.ebeaninternal.server.lib.BackgroundThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages access to named DataSources.
 */
public class DataSourceManager implements DataSourceNotify {
	
	private static final Logger logger = LoggerFactory.getLogger(DataSourceManager.class);
	
    /**
     * An alerter that notifies when the database has problems.
     */
    private final DataSourceAlertListener alertlistener;

    /** 
     * Cache of the named DataSources. 
     */
    private final Hashtable<String,DataSourcePool> dsMap = new Hashtable<String, DataSourcePool>();

    /**
     * Monitor for creating dataSources.
     */
    private final Object monitor = new Object();

    /**
     * The database checker registered with BackgroundThread.
     */
    private final BackgroundRunnable dbChecker;
    
    /**
     * The frequency to test db while it is up.
     */
    private final int dbUpFreqInSecs;
    
    /**
     * The frequency to test db while it is down.
     */
    private final int dbDownFreqInSecs;

    /**
     * Set to true when shutting down.
     */
    private boolean shuttingDown;
    	
    private boolean deregisterDriver;
    
	/** 
	 * Construct with explicit ConfigProperties.
	 */
	public DataSourceManager() {
		
		this.alertlistener = createAlertListener();
		
		// perform heart beat every 30 seconds by default
        this.dbUpFreqInSecs = GlobalProperties.getInt("datasource.heartbeatfreq",30);
        this.dbDownFreqInSecs = GlobalProperties.getInt("datasource.deadbeatfreq",10);        
        this.dbChecker = new BackgroundRunnable(new Checker(), dbUpFreqInSecs);
		this.deregisterDriver = GlobalProperties.getBoolean("datasource.deregisterDriver", true);
        
		try {
	        BackgroundThread.add(dbChecker);
            		    
		} catch (Exception e) {
			logger.error(null, e);
		}
	}

	private DataSourceAlertListener createAlertListener() throws DataSourceException {
		
		String alertCN = GlobalProperties.get("datasource.alert.class", null);
		if (alertCN == null){
			return new SimpleAlerter();
			
		} else {
		    try {
		        return (DataSourceAlertListener)ClassUtil.newInstance(alertCN, this.getClass());
		        
		    } catch (Exception ex){
		    	throw new DataSourceException(ex);
		    }
		}
	}

    /**
     * Send an alert to say the dataSource is back up.
     */
	public void notifyDataSourceUp(String dataSourceName){

        dbChecker.setFreqInSecs(dbUpFreqInSecs);
		
		if (alertlistener != null){
		    alertlistener.dataSourceUp(dataSourceName);
		}
	}

    /**
     * Send an alert to say the dataSource is down.
     */
	public void notifyDataSourceDown(String dataSourceName){
		
        dbChecker.setFreqInSecs(dbDownFreqInSecs);
        
		if (alertlistener != null){
		    alertlistener.dataSourceDown(dataSourceName); 
		}
	}

    /**
     * Send an alert to say the dataSource is getting close to its max size.
     */
	public void notifyWarning(String subject, String msg){
		if (alertlistener != null){
		    alertlistener.warning(subject, msg);
		}
	}

    /**
     * Return true when the dataSource is shutting down.
     */
	public boolean isShuttingDown() {
		synchronized(monitor) {
			return shuttingDown;
		}
	}
	
    /**
     * Shutdown the dataSources.
     */
    public void shutdown() {
		
		synchronized(monitor) {
			
			this.shuttingDown = true;
			
			Collection<DataSourcePool> values = dsMap.values();
			for (DataSourcePool ds : values) {
				try {					
					ds.shutdown();
				} catch (DataSourceException e) {
					// should never be thrown as the DataSources are all created...
					logger.error(null, e);
				}
            }
			if (deregisterDriver){
				for (DataSourcePool ds : values) {
	                ds.deregisterDriver();
                }
			}
		}
	}

    /**
     * Return the DataSourcePool's.
     */
	public List<DataSourcePool> getPools() {
		synchronized(monitor) {
			// create a copy of the DataSourcePool's
			ArrayList<DataSourcePool> list = new ArrayList<DataSourcePool>();
			list.addAll(dsMap.values());
			return list;
		}
	}
    
	/**
	 * Get the dataSource using the default ConfigProperties.
	 */
	public DataSourcePool getDataSource(String name) {
		return getDataSource(name, null);
	}
	
	
	public DataSourcePool getDataSource(String name, DataSourceConfig dsConfig){
		
		if (name == null){
			throw new IllegalArgumentException("name not defined");
		}
				
	    synchronized(monitor){
		    DataSourcePool pool = dsMap.get(name);
		    if (pool == null){
		    	if (dsConfig == null){
					dsConfig = new DataSourceConfig();
					dsConfig.loadSettings(name);
				}
		        pool = new DataSourcePool(this, name, dsConfig);
		        dsMap.put(name, pool); 
		    }
		    return pool;
		}
	}
	
	/**
	 * Check that the database is up by performing a simple query. This should
	 * be done periodically. By default every 30 seconds.
	 */
	private void checkDataSource() {

		synchronized (monitor) {
			if (!isShuttingDown()) {
				Iterator<DataSourcePool> it = dsMap.values().iterator();
				while (it.hasNext()) {
					DataSourcePool ds = it.next();
					ds.checkDataSource();
				}
			}
		}
	}
    
    /**
     * Runs every dbUpFreqInSecs to make sure dataSource is up.
     */
    private final class Checker implements Runnable {

        public void run() {
            checkDataSource();
        }
    }
}
