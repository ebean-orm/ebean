package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.SqlQueryListener;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.SpiSqlQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.Message;
import com.avaje.ebeaninternal.server.core.RelationalQueryEngine;
import com.avaje.ebeaninternal.server.core.RelationalQueryRequest;
import com.avaje.ebeaninternal.server.lib.util.Str;
import com.avaje.ebeaninternal.server.persist.Binder;
import com.avaje.ebeaninternal.server.transaction.TransactionManager;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.util.BindParamsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Perform native sql fetches.
 */
public class DefaultRelationalQueryEngine implements RelationalQueryEngine {

	private static final Logger logger = LoggerFactory.getLogger(DefaultRelationalQueryEngine.class);

  private static final int GLOBAL_ROW_LIMIT = Integer.valueOf(System.getProperty("ebean.query.globallimit","1000000"));

	private final Binder binder;
	
	private final String dbTrueValue;

	public DefaultRelationalQueryEngine(Binder binder, String dbTrueValue) {
		this.binder = binder;
		this.dbTrueValue = dbTrueValue == null ? "true" : dbTrueValue;
	}

	public Object findMany(RelationalQueryRequest request) {

		SpiSqlQuery query = request.getQuery();

		long startTime = System.currentTimeMillis();

		SpiTransaction t = request.getTransaction();
		Connection conn = t.getInternalConnection();
		ResultSet rset = null;
		PreparedStatement pstmt = null;

		String sql = query.getQuery();

		BindParams bindParams = query.getBindParams();

		if (!bindParams.isEmpty()) {
			// convert any named parameters if required
			sql = BindParamsParser.parse(bindParams, sql);
		}

		try {

			String bindLog = "";
			String[] propNames;
			
			synchronized (query) {
				if (query.isCancelled()){
					logger.trace("Query already cancelled");
					return null;
				}
				
				// synchronise for query.cancel() support		
				pstmt = conn.prepareStatement(sql);
	
				if (query.getTimeout() > 0){
					pstmt.setQueryTimeout(query.getTimeout());
				}
				if (query.getBufferFetchSizeHint() > 0){
					pstmt.setFetchSize(query.getBufferFetchSizeHint());
				}
				
				if (!bindParams.isEmpty()) {
					bindLog = binder.bind(bindParams, new DataBind(pstmt));
				}
	
				if (request.isLogSql()) {
				  String logSql = sql;
				  if (TransactionManager.SQL_LOGGER.isTraceEnabled()) {
				    logSql = Str.add(logSql, "; --bind(", bindLog, ")");
				  }
					t.logSql(logSql);
				}
	
				rset = pstmt.executeQuery();
	
				propNames = getPropertyNames(rset);
			}
			
			// calculate the initialCapacity of the Map to reduce
			// rehashing for queries with 12+ columns
			float initCap = (propNames.length) / 0.7f;
			int estimateCapacity = (int) initCap + 1;

			// determine the maxRows limit
			int maxRows = GLOBAL_ROW_LIMIT;
			if (query.getMaxRows() >= 1) {
				maxRows = query.getMaxRows();
			}

			int loadRowCount = 0;

			SqlQueryListener listener = query.getListener();

			BeanCollectionWrapper wrapper = new BeanCollectionWrapper(request);
			boolean isMap = wrapper.isMap();
			String mapKey = query.getMapKey();
			
			SqlRow bean = null;
			
			while (rset.next()) {
				synchronized (query) {					
					// synchronise for query.cancel() support		
					if (!query.isCancelled()){
						bean = readRow(rset, propNames, estimateCapacity);
					}
				}
				if (bean != null){
					// bean can be null if query cancelled
					if (listener != null) {
						listener.process(bean);
	
					} else {
						if (isMap) {
							Object keyValue = bean.get(mapKey);
							wrapper.addToMap(bean, keyValue);
						} else {
							wrapper.addToCollection(bean);
						}
					}
	
					loadRowCount++;
	
					if (loadRowCount == maxRows) {
						// break, as we have hit the max rows to fetch...
						break;
					}
				}
			}

			BeanCollection<?> beanColl = wrapper.getBeanCollection();

			if (request.isLogSummary()) {
				long exeTime = System.currentTimeMillis() - startTime;
				String msg = "SqlQuery  rows[" + loadRowCount + "] time[" + exeTime + "] bind[" + bindLog + "]";
				t.logSummary(msg);
			}
			
			if (query.isCancelled()){
				logger.debug("Query was cancelled during execution rows:"+loadRowCount);
			}
			
			return beanColl;

		} catch (Exception e) {
			String m = Message.msg("fetch.error", e.getMessage(), sql);
			throw new PersistenceException(m, e);

		} finally {
			try {
				if (rset != null) {
					rset.close();
				}
			} catch (SQLException e) {
				logger.error(null, e);
			}
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException e) {
				logger.error(null, e);
			}
		}
	}

	/**
	 * Build the list of property names.
	 */
	protected String[] getPropertyNames(ResultSet rset) throws SQLException {

		ArrayList<String> propNames = new ArrayList<String>();

		ResultSetMetaData rsmd = rset.getMetaData();

		int columnsPlusOne = rsmd.getColumnCount()+1;

		
		for (int i = 1; i < columnsPlusOne; i++) {
			String columnName = rsmd.getColumnLabel(i);
			// will convert columnName to lower case
			propNames.add(columnName);
		}

		return propNames.toArray(new String[propNames.size()]);
	}

	/**
	 * Read the row from the ResultSet and return as a MapBean.
	 */
	protected SqlRow readRow(ResultSet rset, String[] propNames, int initialCapacity) throws SQLException {

		// by default a map will rehash on the 12th entry
		// it will be pretty common to have 12 or more entries so
		// to reduce rehashing I am trying to estimate a good
		// initial capacity for the MapBean to use.
		SqlRow bean = new DefaultSqlRow(initialCapacity, 0.75f, dbTrueValue);
		
		int index = 0;

		for (int i = 0; i < propNames.length; i++) {
			index++;
			Object value = rset.getObject(index);
			bean.set(propNames[i], value);
		}

		return bean;

	}

}
