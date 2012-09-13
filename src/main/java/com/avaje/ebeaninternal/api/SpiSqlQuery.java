package com.avaje.ebeaninternal.api;

import java.sql.PreparedStatement;

import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlQueryListener;

/**
 * SQL query - Internal extension to SqlQuery.
 */
public interface SpiSqlQuery extends SqlQuery {

	/**
	 * Return the named or positioned parameters.
	 */
	public BindParams getBindParams();

	/**
	 * return the query.
	 */
	public String getQuery();

	/**
	 * Return the queryListener.
	 */
	public SqlQueryListener getListener();
	
	/**
	 * Return the first row to fetch.
	 */
    public int getFirstRow();
    
    /**
     * Return the maximum number of rows to fetch.
     */
	public int getMaxRows();
	
	/**
	 * Return the number of rows after which background fetching occurs.
	 */
	public int getBackgroundFetchAfter();
	
	/**
	 * Return the key property for maps.
	 */
	public String getMapKey();
	
	/**
	 * Return the query timeout.
	 */
	public int getTimeout();

	/**
	 * Return the hint for Statement.setFetchSize().
	 */
	public int getBufferFetchSizeHint();

	/**
	 * Return true if this is a future fetch type query.
	 */
	public boolean isFutureFetch();

	/**
	 * Set to true if this is a future fetch type query.
	 */
	public void setFutureFetch(boolean futureFetch);

	/**
	 * Set the PreparedStatement for the purposes of supporting cancel.
	 */
	public void setPreparedStatement(PreparedStatement pstmt);
	
	/**
	 * Return true if the query has been cancelled.
	 */
	public boolean isCancelled();
}
