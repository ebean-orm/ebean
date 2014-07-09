package com.avaje.ebeaninternal.server.querydefn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlFutureList;
import com.avaje.ebean.SqlQueryListener;
import com.avaje.ebean.SqlRow;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.SpiSqlQuery;

/**
 * Default implementation of SQuery - SQL Query.
 */
public class DefaultRelationalQuery implements SpiSqlQuery {

	private static final long serialVersionUID = -1098305779779591068L;

	private transient EbeanServer server;

	private transient SqlQueryListener queryListener;

	private String query;
		
	private int firstRow;
	
	private int maxRows;

	private int timeout;
	
	private boolean futureFetch;
	
	private boolean cancelled;
	
	/**
	 * For the purposes of cancelling the query.
	 */
	private transient PreparedStatement pstmt;
	
    /**
	 * The rows after which the fetch continues in a bg thread.
	 */
	private int backgroundFetchAfter;

	private int bufferFetchSizeHint;
	
	/**
	 * The property used to get the key value for a Map.
	 */
	private String mapKey;
	    
    /**
     * Bind parameters when using the query language.
     */
	private BindParams bindParams = new BindParams();
    
	/**
	 * Additional supply a query detail object.
	 */
	public DefaultRelationalQuery(EbeanServer server, String query) {
		this.server = server;
		this.query = query;
	}
    
	public DefaultRelationalQuery setQuery(String query) {
		this.query = query;
		return this;
	}
	
	public List<SqlRow> findList() {
		return server.findList(this, null);
	}	
    
	public Set<SqlRow> findSet() {
		return server.findSet(this, null);
	}
	
	public Map<?,SqlRow> findMap() {
		return server.findMap(this, null);
	}
	
	public SqlRow findUnique() {
		return server.findUnique(this, null);
	}

	public SqlFutureList findFutureList() {
		return server.findFutureList(this, null);
	}

	public DefaultRelationalQuery setParameter(int position, Object value) {
        bindParams.setParameter(position, value);
        return this;
    }
    
    public DefaultRelationalQuery setParameter(String paramName, Object value) {
    	 bindParams.setParameter(paramName, value);
        return this;
    }
    
    /**
     * Return the findListener is one has been set.
     */
    public SqlQueryListener getListener() {
		return queryListener;
	}

    /**
     * Set a listener. This is designed for large fetches
     * where lots are rows are to be processed and instead of
     * returning all the rows they are processed one at a time.
     * <p>
     * Note that the returning List Set or Map will be empty.
     * </p>
     */
	public DefaultRelationalQuery setListener(SqlQueryListener queryListener) {
		this.queryListener = queryListener;
		return this;
	}
    
    public String toString() {
    	return "SqlQuery ["+query+"]";
    }

	public int getFirstRow() {
		return firstRow;
	}

	public DefaultRelationalQuery setFirstRow(int firstRow) {
		this.firstRow = firstRow;
		return this;
	}

	public int getMaxRows() {
		return maxRows;
	}

	public DefaultRelationalQuery setMaxRows(int maxRows) {
		this.maxRows = maxRows;
		return this;
	}

	public String getMapKey() {
		return mapKey;
	}

	public DefaultRelationalQuery setMapKey(String mapKey) {
		this.mapKey = mapKey;
		return this;
	}

	public int getBackgroundFetchAfter() {
		return backgroundFetchAfter;
	}

	public DefaultRelationalQuery setBackgroundFetchAfter(int backgroundFetchAfter) {
		this.backgroundFetchAfter = backgroundFetchAfter;
		return this;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	public DefaultRelationalQuery setTimeout(int secs) {
		this.timeout = secs;
		return this;
	}

	public BindParams getBindParams() {
		return bindParams;
	}

	public DefaultRelationalQuery setBufferFetchSizeHint(int bufferFetchSizeHint){
		this.bufferFetchSizeHint = bufferFetchSizeHint;
		return this;
	}

	
	public int getBufferFetchSizeHint() {
		return bufferFetchSizeHint;
	}
	
	public String getQuery() {
		return query;
	}

	public boolean isFutureFetch() {
		return futureFetch;
	}

	public void setFutureFetch(boolean futureFetch) {
		this.futureFetch = futureFetch;
	}

	public void setPreparedStatement(PreparedStatement pstmt){
		synchronized (this) {
			this.pstmt = pstmt;
		}
	}
	
	public void cancel() {
		synchronized (this) {
			this.cancelled = true;
			if (pstmt != null){
				try {
					pstmt.cancel();
				} catch (SQLException e) {
					String msg = "Error cancelling query";
					throw new PersistenceException(msg, e);
				}
			}
		}
	}
	
	public boolean isCancelled() {
		synchronized (this) {
			return cancelled;
		}
	}    

}
