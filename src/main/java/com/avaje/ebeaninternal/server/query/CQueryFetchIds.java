package com.avaje.ebeaninternal.server.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.BeanIdList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiQuery.Mode;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.core.SpiOrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.type.DataReader;
import com.avaje.ebeaninternal.server.type.RsetDataReader;

/**
 * Executes the select row count query.
 */
public class CQueryFetchIds {

	private static final Logger logger = LoggerFactory.getLogger(CQueryFetchIds.class);

	/**
	 * The overall find request wrapper object.
	 */
	private final OrmQueryRequest<?> request;

	private final BeanDescriptor<?> desc;

	private final SpiQuery<?> query;

	/**
	 * Where clause predicates.
	 */
	private final CQueryPredicates predicates;

	/**
	 * The final sql that is generated.
	 */
	private final String sql;

	private RsetDataReader dataReader;
	
	/**
	 * The statement used to create the resultSet.
	 */
	private PreparedStatement pstmt;

	private String bindLog;

	private long startNano;
	
	private int executionTimeMicros;

	private int rowCount;
	
	private final int maxRows;
	
	/**
	 * Create the Sql select based on the request.
	 */
	public CQueryFetchIds(OrmQueryRequest<?> request, CQueryPredicates predicates, String sql) {

		this.request = request;
		this.query = request.getQuery();
		this.sql = sql;
		this.maxRows = query.getMaxRows();

		query.setGeneratedSql(sql);

		this.desc = request.getBeanDescriptor();
		this.predicates = predicates;

	}
	
	/**
	 * Return a summary description of this query.
	 */
	public String getSummary() {
		StringBuilder sb = new StringBuilder();
			sb.append("FindIds exeMicros[").append(executionTimeMicros)
			.append("] rows[").append(rowCount)
			.append("] type[").append(desc.getName())
			.append("] predicates[").append(predicates.getLogWhereSql())
			.append("] bind[").append(bindLog).append("]");
		
		return sb.toString();		
	}

	/**
	 * Return the bind log.
	 */
	public String getBindLog() {
        return bindLog;
    }

    /**
	 * Return the generated sql.
	 */
	public String getGeneratedSql() {
		return sql;
	}
	
	public SpiOrmQueryRequest<?> getQueryRequest() {
		return request;
	}

	/**
	 * Execute the query returning the row count.
	 */
	public BeanIdList findIds() throws SQLException {

		startNano = System.nanoTime();
		
		try {
			// get the list that we are going to put the id's into.
			// This was already set so that it is available to be 
			// read by other threads (it is a synchronised list)
			List<Object> idList = query.getIdList();
			if (idList == null){
				// running in foreground thread (not FutureIds query)
				idList = Collections.synchronizedList(new ArrayList<Object>());
				query.setIdList(idList);
			}
			
			BeanIdList result = new BeanIdList(idList);
			
			SpiTransaction t = request.getTransaction();
			Connection conn = t.getInternalConnection();
			pstmt = conn.prepareStatement(sql);
			
			if (query.getBufferFetchSizeHint() > 0){
				pstmt.setFetchSize(query.getBufferFetchSizeHint());
			}
	
			if (query.getTimeout() > 0){
				pstmt.setQueryTimeout(query.getTimeout());
			}
	
			bindLog = predicates.bind(new DataBind(pstmt));
	
			ResultSet rset = pstmt.executeQuery();
			dataReader = new RsetDataReader(rset);
		
			boolean hitMaxRows = false;
			boolean hasMoreRows = false;
			rowCount = 0;
			
			DbReadContext ctx = new DbContext();
			
			while (rset.next()){
				Object idValue = desc.getIdBinder().read(ctx);
				idList.add(idValue);
				// reset back to 0
				dataReader.resetColumnPosition();
				rowCount++;
				
				if (maxRows > 0 && rowCount == maxRows) {
					hitMaxRows = true;
					hasMoreRows = rset.next();
					break;

				}
			}
			
			if (hitMaxRows){
				result.setHasMore(hasMoreRows);
			}
			
			long exeNano = System.nanoTime() - startNano;
			executionTimeMicros = (int)exeNano/1000;

			return result;
			
		} finally {
			close();
		}
	}

	/**
	 * Close the resources.
	 * <p>
	 * The jdbc resultSet and statement need to be closed. Its important that
	 * this method is called.
	 * </p>
	 */
	private void close() {
		try {
			if (dataReader != null) {
			    dataReader.close();
			    dataReader = null;
			}
		} catch (SQLException e) {
			logger.error(null, e);
		}
		try {
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
		} catch (SQLException e) {
			logger.error(null, e);
		}
	}

	
	class DbContext implements DbReadContext {

        public void propagateState(Object e) {
            throw new RuntimeException("Not Called");
        }

        public Mode getQueryMode() {
			return Mode.NORMAL;
		}
		
		public DataReader getDataReader() {
            return dataReader;
        }

		public Boolean isReadOnly() {
			return Boolean.FALSE;
		}
		
		public boolean isRawSql() {
			return false;
		}

		public void register(String path, EntityBeanIntercept ebi){
		}

		public void register(String path, BeanCollection<?> bc){	
		}

		public BeanPropertyAssocMany<?> getManyProperty() {
			// always null
			return null;
		}

		public PersistenceContext getPersistenceContext() {
			// always null
			return null;
		}

		public boolean isAutoFetchProfiling() {
			return false;
		}

		public void profileBean(EntityBeanIntercept ebi, String prefix) {
			// no-op			
		}

		public void setCurrentPrefix(String currentPrefix,Map<String, String> pathMap) {
			// no-op
		}

		public void setLoadedBean(EntityBean loadedBean, Object id, Object lazyLoadParentId) {
			// no-op
		}

		public void setLoadedManyBean(EntityBean loadedBean) {
			// no-op
		}
		
	}
	
}
