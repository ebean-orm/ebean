package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.OptimisticLockException;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.core.PstmtBatch;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.persist.BatchedPstmt;
import com.avaje.ebeaninternal.server.persist.BatchedPstmtHolder;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableRequest;
import com.avaje.ebeaninternal.server.type.DataBind;


/**
 * Base class for Handler implementations.
 */
public abstract class DmlHandler implements PersistHandler, BindableRequest {

    protected static final Logger logger = Logger.getLogger(DmlHandler.class.getName());
	
	/**
	 * The originating request.
	 */
	protected final PersistRequestBean<?> persistRequest;

	protected final StringBuilder bindLog;

	protected final Set<String> loadedProps;

	protected final SpiTransaction transaction;
	
	protected final boolean emptyStringToNull;
	
    protected final boolean logLevelSql;

	/**
	 * The PreparedStatement used for the dml.
	 */
    protected DataBind dataBind;
    
    protected String sql;
    
    protected ArrayList<UpdateGenValue> updateGenValues;
    
    private Set<String> additionalProps;

//    private boolean checkDelta;
//
//    private BeanDelta deltaBean;

  protected DmlHandler(PersistRequestBean<?> persistRequest, boolean emptyStringToNull) {
    this.persistRequest = persistRequest;
    this.emptyStringToNull = emptyStringToNull;
    this.loadedProps = persistRequest.getLoadedProperties();
    this.transaction = persistRequest.getTransaction();
    this.logLevelSql = transaction.isLogSql();
    if (logLevelSql) {
      this.bindLog = new StringBuilder();
    } else {
      this.bindLog = null;
    }
  }

//	protected void setCheckDelta(boolean checkDelta) {
//        this.checkDelta = checkDelta;
//    }

  public PersistRequestBean<?> getPersistRequest() {
    return persistRequest;
  }
	
	/**
	 * Get the sql and bind the statement.
	 */
	public abstract void bind() throws SQLException;

	/**
	 * Execute now for non-batch execution.
	 */
	public abstract void execute() throws SQLException;

	/**
	 * Check the rowCount.
	 */
	protected void checkRowCount(int rowCount) throws SQLException, OptimisticLockException {
		try {
			persistRequest.checkRowCount(rowCount);
			persistRequest.postExecute();
		} catch (OptimisticLockException e){
			// add the SQL and bind values to error message 
			String m = e.getMessage()+" sql["+sql+"] bind["+bindLog+"]";
			persistRequest.getTransaction().log("OptimisticLockException:"+m);
			throw new OptimisticLockException(m, null, e.getEntity());
		}
	}
	
	/**
	 * Add this for batch execution.
	 */
	public void addBatch() throws SQLException {
		PstmtBatch pstmtBatch = persistRequest.getPstmtBatch();
		if (pstmtBatch != null){
			pstmtBatch.addBatch(dataBind.getPstmt());
		} else {
		    dataBind.getPstmt().addBatch();
		}
	}

	/**
	 * Close the underlying statement.
	 */
	public void close() {
		try {
			if (dataBind != null){
			    dataBind.close();
			}
		} catch (SQLException ex) {
        	logger.log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Return the bind log.
	 */
	public String getBindLog() {
	    return bindLog == null ? "" : bindLog.toString();
	}
	
	/**
	 * Set the Id value that was bound. This value is used for logging summary
	 * level information.
	 */
	public void setIdValue(Object idValue) {
		persistRequest.setBoundId(idValue);
	}

	/**
	 * Log the bind information to the transaction log.
	 */
	protected void logBinding() {
		if (logLevelSql) {
		    transaction.logInternal(bindLog.toString());
		}
	}

	/**
	 * Log the sql to the transaction log.
	 */
	protected void logSql(String sql) {
		if (logLevelSql) {
			transaction.logInternal(sql);
		}
	}
	
	
	public boolean isIncluded(BeanProperty prop) {
		return (loadedProps == null || loadedProps.contains(prop.getName()));
	}
	
    public boolean isIncludedWhere(BeanProperty prop) {
        if (prop.isDbEncrypted()){
            // update without a version property ...
            // for encrypted properties only include if it was 
            // also an updated/modified property
            return isIncluded(prop);
        }
        return prop.isDbUpdatable() && (loadedProps == null || loadedProps.contains(prop.getName()));    
    }
    
    /**
	 * Bind a raw value. Used to bind the discriminator column.
	 */
	public Object bind(String propName, Object value, int sqlType) throws SQLException {
		if (logLevelSql) {
			bindLog.append(propName).append("=");
			bindLog.append(value).append(", ");
		}
		dataBind.setObject(value, sqlType);
		return value;
	}

    public Object bindNoLog(Object value, int sqlType, String logPlaceHolder) throws SQLException {
        if (logLevelSql) {
            bindLog.append(logPlaceHolder).append(" ");
        }
        dataBind.setObject(value, sqlType);
        return value;
    }

    /**
     * Bind the value to the preparedStatement.
     */
    public Object bind(Object value, BeanProperty prop, String propName, boolean bindNull) throws SQLException {
        return bindInternal(logLevelSql, value, prop, propName, bindNull);
    }
    
    /**
     * Bind the value to the preparedStatement without logging.
     */
    public Object bindNoLog(Object value, BeanProperty prop, String propName, boolean bindNull) throws SQLException {
        return bindInternal(false, value, prop, propName, bindNull);
    }
    
	private Object bindInternal(boolean log, Object value, BeanProperty prop, String propName, boolean bindNull) throws SQLException {
				
		if (!bindNull){
		    if (emptyStringToNull && (value instanceof String) && ((String)value).length() == 0){
	            // support Oracle conversion of empty string to null 
	            //value = prop.getDbNullValue(value);
		        value = null;
		    }
		}

		if (!bindNull && value == null) { 
			// where will have IS NULL clause so don't actually bind
			if (log) {
				bindLog.append(propName).append("=");
				bindLog.append("null, ");
			}
		} else {
			if (log) {
				bindLog.append(propName).append("=");
				if (prop.isLob()){
					bindLog.append("[LOB]");
				} else {
					String sv = String.valueOf(value);
					if (sv.length() > 50){
						sv = sv.substring(0,47)+"...";
					}
					bindLog.append(sv);
				}
				bindLog.append(", ");
			}
			// do the actual binding to PreparedStatement
			prop.bind(dataBind, value);
//			if (checkDelta) {
//			    if (!prop.isId() && prop.isDeltaRequired()){
//			        if (deltaBean == null){
//			            deltaBean = persistRequest.createDeltaBean();
//			            transaction.getEvent().addBeanDelta(deltaBean);
//			        }
//			        deltaBean.add(prop, value);
//			    }
//			}
		}
		return value;
	}
	
	/**
	 * Add the comment to the bind information log.
	 */
	protected void bindLogAppend(String comment) {
		if (logLevelSql) {
			bindLog.append(comment);
		}
	}

    /**
     * For generated properties set on insert register as additional
     * loaded properties if required.
     */
    public final void registerAdditionalProperty(String propertyName) {
        if (loadedProps != null && !loadedProps.contains(propertyName)){
            if (additionalProps == null){
                additionalProps = new HashSet<String>();
            }
            additionalProps.add(propertyName);
        }
    }

    /**
     * Set any additional (generated) properties to the set of loaded properties
     * if required.
     */
    protected void setAdditionalProperties() {
        if (additionalProps != null){
            // additional generated properties set on insert
            // added to the set of loaded properties
            additionalProps.addAll(loadedProps);
            persistRequest.setLoadedProps(additionalProps);
        }
    }

    /**
	 * Register a generated value on a update. This can not be set to the bean
	 * until after the where clause has been bound for concurrency checking.
	 * <p>
	 * GeneratedProperty values are likely going to be used for optimistic
	 * concurrency checking. This includes 'counter' and 'update timestamp'
	 * generation.
	 * </p>
	 */
	public void registerUpdateGenValue(BeanProperty prop, Object bean, Object value) {
		if (updateGenValues == null) {
			updateGenValues = new ArrayList<UpdateGenValue>();
		}
		updateGenValues.add(new UpdateGenValue(prop, bean, value));
        registerAdditionalProperty(prop.getName());
	}



	/**
	 * Set any update generated values to the bean. Must be called after where
	 * clause has been bound.
	 */
	public void setUpdateGenValues() {
		if (updateGenValues != null) {
			for (int i = 0; i < updateGenValues.size(); i++) {
				UpdateGenValue updGenVal = updateGenValues.get(i);
				updGenVal.setValue();
			}
		}
	}

	
	/**
	 * Check with useGeneratedKeys to get appropriate PreparedStatement.
	 */
	protected PreparedStatement getPstmt(SpiTransaction t, String sql, boolean genKeys) throws SQLException {
		Connection conn = t.getInternalConnection();
		if (genKeys) {
			// the Id generated is always the first column
			// Required to stop Oracle10 giving us Oracle rowId??
			// Other jdbc drivers seem fine without this hint.
            int[] columns = {1};
            return conn.prepareStatement(sql, columns);
            
        } else {
            return conn.prepareStatement(sql);
        }
	}

	/**
	 * Return a prepared statement taking into account batch requirements.
	 */
	protected PreparedStatement getPstmt(SpiTransaction t, String sql, PersistRequestBean<?> request, boolean genKeys)
			throws SQLException {

		BatchedPstmtHolder batch = t.getBatchControl().getPstmtHolder();
		PreparedStatement stmt = batch.getStmt(sql, request);

		if (stmt != null) {
			return stmt;
		}

		if (logLevelSql){
		    t.logInternal(sql);
		}
		
		stmt = getPstmt(t, sql, genKeys);

		PstmtBatch pstmtBatch = request.getPstmtBatch();
		if (pstmtBatch != null){
        	pstmtBatch.setBatchSize(stmt, t.getBatchControl().getBatchSize());
        }
		
		BatchedPstmt bs = new BatchedPstmt(stmt, genKeys, sql, request.getPstmtBatch(), true);
		batch.addStmt(bs, request);
		return stmt;
	}
	
	/**
	 * Hold the values from GeneratedValue that need to be set to the bean
	 * property after the where clause has been built.
	 */
	private static final class UpdateGenValue {

		private final BeanProperty property;

		private final Object bean;

		private final Object value;

		private UpdateGenValue(BeanProperty property, Object bean, Object value) {
			this.property = property;
			this.bean = bean;
			this.value = value;
		}

		/**
		 * Set the value to the bean property.
		 */
		private void setValue() {
			// support PropertyChangeSupport
			property.setValueIntercept(bean, value);
		}
	}
}
