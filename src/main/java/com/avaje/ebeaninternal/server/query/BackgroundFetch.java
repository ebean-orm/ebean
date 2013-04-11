package com.avaje.ebeaninternal.server.query;

import java.util.concurrent.Callable;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebeaninternal.api.SpiTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Continue the fetch using a Background thread. The client knows when this has
 * finished by checking to see if beanList.finishedFetch() is true.
 */
public class BackgroundFetch implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(BackgroundFetch.class);
	
	private final CQuery<?> cquery;
    
    private final SpiTransaction transaction;
    
    /**
     * Create the BackgroundFetch.
     */
    public BackgroundFetch(CQuery<?> cquery) {
        this.cquery = cquery;  
        this.transaction = cquery.getTransaction();
    }

    /**
     * Continue the fetch.
     */
    public Integer call() {
        try {
            
        	BeanCollection<?> bc = cquery.continueFetchingInBackground();
        	
        	return bc.size();
            
        } catch (Exception e) {
        	logger.error(null, e);
        	return Integer.valueOf(0);
        	
        } finally {
            try {
            	cquery.close();
            } catch (Exception e) {
            	logger.error(null, e);
            }
            try {
            	// we must have our own transaction for background fetching
            	// and this performs the rollback...  returning the 
            	// connection back into the connection pool.
            	transaction.rollback();
            } catch (Exception e) {
            	logger.error(null, e);
            }
        }

    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("BackgroundFetch ").append(cquery);
        return sb.toString();
    }

}
