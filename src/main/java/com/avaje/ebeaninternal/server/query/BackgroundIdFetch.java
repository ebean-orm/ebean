package com.avaje.ebeaninternal.server.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebeaninternal.api.BeanIdList;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;

/**
 * Continue the fetch using a Background thread. The client knows when this has
 * finished by checking to see if beanList.finishedFetch() is true.
 */
public class BackgroundIdFetch implements Callable<Integer> {

	private static final Logger logger = Logger.getLogger(BackgroundIdFetch.class.getName());
	
	private final ResultSet rset;
    
	private final PreparedStatement pstmt;
	
    private final SpiTransaction transaction;
    
    private final DbReadContext ctx;
    
    private final BeanDescriptor<?> beanDescriptor;
    
    private final BeanIdList idList;
    /**
     * Create the BackgroundFetch.
     */
    public BackgroundIdFetch(SpiTransaction transaction,
    		ResultSet rset, PreparedStatement pstmt,
    		DbReadContext ctx, BeanDescriptor<?> beanDescriptor,
    		BeanIdList idList) {
    	
    	this.ctx = ctx;
        this.transaction = transaction;
        this.rset = rset;  
        this.pstmt = pstmt;
        this.beanDescriptor = beanDescriptor;
        this.idList = idList;
    }

    /**
     * Continue the fetch.
     */
    public Integer call() {
        try {
        	int startSize = idList.getIdList().size();
            int rowsRead = 0;
        	while (rset.next()){
				Object idValue = beanDescriptor.getIdBinder().read(ctx);
				idList.add(idValue);
				ctx.getDataReader().resetColumnPosition();
				rowsRead++;				
			}
        	
        	if (logger.isLoggable(Level.INFO)){
        		logger.info("BG FetchIds read:"+rowsRead+" total:"+(startSize+rowsRead));
        	}
        	
        	return rowsRead;
            
        } catch (Exception e) {
        	logger.log(Level.SEVERE, null, e);
        	return 0;
        	
        } finally {
            try {
            	close();
            } catch (Exception e) {
            	logger.log(Level.SEVERE, null, e);
            }
            try {
            	// we must have our own transaction for background fetching
            	// and this performs the rollback...  returning the 
            	// connection back into the connection pool.
            	transaction.rollback();
            } catch (Exception e) {
            	logger.log(Level.SEVERE, null, e);
            }
        }

    }

	private void close() {
		try {
			if (rset != null) {
				rset.close();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, null, e);
		}
		try {
			if (pstmt != null) {
				pstmt.close();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

}
