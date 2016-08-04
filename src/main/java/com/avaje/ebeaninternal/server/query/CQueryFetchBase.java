package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiQuery.Mode;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.type.DataReader;
import com.avaje.ebeaninternal.server.type.RsetDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Base compiled query request for single attribute queries.
 */
public abstract class CQueryFetchBase {

  private static final Logger logger = LoggerFactory.getLogger(CQueryFetchBase.class);

  /**
   * The overall find request wrapper object.
   */
  protected final OrmQueryRequest<?> request;

  protected final BeanDescriptor<?> desc;

  protected final SpiQuery<?> query;

  /**
   * Where clause predicates.
   */
  protected final CQueryPredicates predicates;

  /**
   * The final sql that is generated.
   */
  protected final String sql;

  protected RsetDataReader dataReader;

  /**
   * The statement used to create the resultSet.
   */
  protected PreparedStatement pstmt;

  protected String bindLog;

  protected int executionTimeMicros;

  protected int rowCount;

  protected final int maxRows;

  /**
   * Create the Sql select based on the request.
   */
  public CQueryFetchBase(OrmQueryRequest<?> request, CQueryPredicates predicates, String sql) {

    this.request = request;
    this.query = request.getQuery();
    this.sql = sql;
    this.maxRows = query.getMaxRows();

    query.setGeneratedSql(sql);

    this.desc = request.getBeanDescriptor();
    this.predicates = predicates;
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

  protected ResultSet prepareExecute() throws SQLException {

    SpiTransaction t = request.getTransaction();
    Connection conn = t.getInternalConnection();
    pstmt = conn.prepareStatement(sql);

    if (query.getBufferFetchSizeHint() > 0) {
      pstmt.setFetchSize(query.getBufferFetchSizeHint());
    }
    if (query.getTimeout() > 0) {
      pstmt.setQueryTimeout(query.getTimeout());
    }

    bindLog = predicates.bind(pstmt, conn);

    ResultSet rset = pstmt.executeQuery();
    dataReader = new RsetDataReader(request.getDataTimeZone(), rset);
    return rset;
  }

  /**
   * Close the resources.
   * <p>
   * The jdbc resultSet and statement need to be closed. Its important that
   * this method is called.
   * </p>
   */
  protected void close() {
    try {
      if (dataReader != null) {
        dataReader.close();
        dataReader = null;
      }
    } catch (SQLException e) {
      logger.error("Error closing DataReader", e);
    }
    try {
      if (pstmt != null) {
        pstmt.close();
        pstmt = null;
      }
    } catch (SQLException e) {
      logger.error("Error closing PreparedStatement", e);
    }
  }


  protected class DbContext implements DbReadContext {

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

    @Override
    public boolean isDisableLazyLoading() {
      return false;
    }

    public boolean isRawSql() {
      return false;
    }

    public void register(String path, EntityBeanIntercept ebi) {
    }

    public void register(String path, BeanCollection<?> bc) {
    }

    public BeanPropertyAssocMany<?> getManyProperty() {
      // always null
      return null;
    }

    public PersistenceContext getPersistenceContext() {
      // always null
      return null;
    }

    public boolean isAutoTuneProfiling() {
      return false;
    }

    public void profileBean(EntityBeanIntercept ebi, String prefix) {
      // no-op
    }

    public void setCurrentPrefix(String currentPrefix, Map<String, String> pathMap) {
      // no-op
    }

    public void setLazyLoadedChildBean(EntityBean loadedBean, Object lazyLoadParentId) {
      // no-op
    }

    @Override
    public boolean isDraftQuery() {
      return false;
    }
  }

}
