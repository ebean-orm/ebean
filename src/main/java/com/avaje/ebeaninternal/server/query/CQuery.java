package com.avaje.ebeaninternal.server.query;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.PersistenceException;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.QueryListener;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.NodeUsageCollector;
import com.avaje.ebean.bean.NodeUsageListener;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.LoadContext;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiQuery.Mode;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.autofetch.AutoFetchManager;
import com.avaje.ebeaninternal.server.core.Message;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.core.SpiOrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanCollectionHelp;
import com.avaje.ebeaninternal.server.deploy.BeanCollectionHelpFactory;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryProperties;
import com.avaje.ebeaninternal.server.transaction.DefaultPersistenceContext;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.type.DataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object that represents a SqlSelect statement.
 * <p>
 * The SqlSelect is based on a tree (Object Graph). The tree is traversed to see
 * what parts are included in the tree according to the value of
 * find.getInclude();
 * </p>
 * <p>
 * The tree structure is flattened into a SqlSelectChain. The SqlSelectChain is
 * the key object used in reading the flat resultSet back into Objects.
 * </p>
 */
public class CQuery<T> implements DbReadContext, CancelableQuery {

  private static final Logger logger = LoggerFactory.getLogger(CQuery.class);

  private static final int GLOBAL_ROW_LIMIT = 1000000;

  /**
   * The resultSet rows read.
   */
  private int rowCount;

  /**
   * The number of master EntityBeans loaded.
   */
  private int loadedBeanCount;

  /**
   * Flag set when no more rows are in the resultSet.
   */
  private boolean noMoreRows;
  /**
   * Id of loaded 'master' bean.
   */
  private Object loadedBeanId;
  /**
   * Flag set when 'master' bean changed.
   */
  boolean loadedBeanChanged;
  /**
   * The 'master' bean just loaded.
   */
  private Object loadedBean;

  /**
   * Holds the previous loaded bean.
   */
  private Object prevLoadedBean;

  /**
   * The detail bean just loaded.
   */
  private Object loadedManyBean;

  /**
   * The previous 'detail' collection remembered so that for manyToMany we can
   * turn on the modify listening.
   */
  private Object prevDetailCollection;

  /**
   * The current 'detail' collection being populated.
   */
  private Object currentDetailCollection;

  /**
   * The 'master' collection being populated.
   */
  private final BeanCollection<T> collection;
  /**
   * The help for the 'master' collection.
   */
  private final BeanCollectionHelp<T> help;

  /**
   * The overall find request wrapper object.
   */
  private final OrmQueryRequest<T> request;

  private final BeanDescriptor<T> desc;

  private final SpiQuery<T> query;

  private final QueryListener<T> queryListener;

  private Map<String, String> currentPathMap;

  private String currentPrefix;

  /**
   * Flag set true when reading 'master' and 'detail' beans.
   */
  private final boolean manyIncluded;

  /**
   * Where clause predicates.
   */
  private final CQueryPredicates predicates;

  /**
   * Object handling the SELECT generation and reading.
   */
  private final SqlTree sqlTree;

  private final boolean rawSql;

  /**
   * The final sql that is generated.
   */
  private final String sql;

  /**
   * Where clause to show in logs when using an existing query plan.
   */
  private final String logWhereSql;

  /**
   * Set to true if the row number column is included in the sql.
   */
  private final boolean rowNumberIncluded;

  /**
   * Tree that knows how to build the master and detail beans from the
   * resultSet.
   */
  private final SqlTreeNode rootNode;

  /**
   * For master detail query.
   */
  private final BeanPropertyAssocMany<?> manyProperty;

  /**
   * The many property Expression language object.
   */
  private final ElPropertyValue manyPropertyEl;

  private final int backgroundFetchAfter;

  private final int maxRowsLimit;

  /**
   * Flag set when backgroundFetchAfter limit is hit.
   */
  private boolean hasHitBackgroundFetchAfter;

  private final PersistenceContext persistenceContext;

  private DataReader dataReader;

  /**
   * The statement used to create the resultSet.
   */
  private PreparedStatement pstmt;

  private boolean cancelled;

  private String bindLog;

  private final CQueryPlan queryPlan;

  private long startNano;

  private final Mode queryMode;

  private final boolean autoFetchProfiling;

  private final ObjectGraphNode autoFetchParentNode;

  private final AutoFetchManager autoFetchManager;
  private final WeakReference<NodeUsageListener> autoFetchManagerRef;

  private int executionTimeMicros;

  private final Boolean readOnly;

  private final SpiExpressionList<?> filterMany;

  /**
   * Create the Sql select based on the request.
   */
  @SuppressWarnings("unchecked")
  public CQuery(OrmQueryRequest<T> request, CQueryPredicates predicates, CQueryPlan queryPlan) {
    this.request = request;
    this.queryPlan = queryPlan;
    this.query = request.getQuery();
    this.queryMode = query.getMode();

    this.readOnly = request.isReadOnly();

    this.autoFetchManager = query.getAutoFetchManager();
    this.autoFetchProfiling = autoFetchManager != null;
    this.autoFetchParentNode = autoFetchProfiling ? query.getParentNode() : null;
    this.autoFetchManagerRef = autoFetchProfiling ? new WeakReference<NodeUsageListener>(
        autoFetchManager) : null;

    // set the generated sql back to the query
    // so its available to the user...
    query.setGeneratedSql(queryPlan.getSql());

    this.sqlTree = queryPlan.getSqlTree();
    this.rootNode = sqlTree.getRootNode();

    this.manyProperty = sqlTree.getManyProperty();
    this.manyPropertyEl = sqlTree.getManyPropertyEl();
    this.manyIncluded = sqlTree.isManyIncluded();
    if (manyIncluded) {
      // get filter to put on the collection for reuse with refresh
      String manyPropertyName = sqlTree.getManyPropertyName();
      OrmQueryProperties chunk = query.getDetail().getChunk(manyPropertyName, false);
      this.filterMany = chunk.getFilterMany();
    } else {
      this.filterMany = null;
    }

    this.sql = queryPlan.getSql();
    this.rawSql = queryPlan.isRawSql();
    this.rowNumberIncluded = queryPlan.isRowNumberIncluded();
    this.logWhereSql = queryPlan.getLogWhereSql();
    this.desc = request.getBeanDescriptor();
    this.predicates = predicates;

    this.queryListener = query.getListener();
    if (queryListener == null) {
      // normal, use the one from the transaction
      this.persistenceContext = request.getPersistenceContext();
    } else {
      // 'Row Level Transaction Context'...
      // local transaction context that will be reset
      // after each 'master' bean is sent to the listener
      this.persistenceContext = new DefaultPersistenceContext();
    }

    this.maxRowsLimit = query.getMaxRows() > 0 ? query.getMaxRows() : GLOBAL_ROW_LIMIT;
    this.backgroundFetchAfter = query.getBackgroundFetchAfter() > 0 ? query
        .getBackgroundFetchAfter() : Integer.MAX_VALUE;

    this.help = createHelp(request);
    this.collection = (BeanCollection<T>) (help != null ? help.createEmpty(false) : null);
  }

  private BeanCollectionHelp<T> createHelp(OrmQueryRequest<T> request) {
    if (request.isFindById()) {
      return null;
    } else {
      SpiQuery.Type manyType = request.getQuery().getType();
      if (manyType == null) {
        // subQuery compiled for InQueryExpression
        return null;
      }
      return BeanCollectionHelpFactory.create(request);
    }
  }

  public Boolean isReadOnly() {
    return readOnly;
  }

  public void propagateState(Object e) {
    if (Boolean.TRUE.equals(readOnly)) {
      if (e instanceof EntityBean) {
        ((EntityBean) e)._ebean_getIntercept().setReadOnly(true);
      }
    }
  }

  public DataReader getDataReader() {
    return dataReader;
  }

  public Mode getQueryMode() {
    return queryMode;
  }

  /**
   * Return true if we want to return vanilla (not enhanced) objects.
   */
  public boolean isVanillaMode() {
    return request.isVanillaMode();
  }

  public CQueryPredicates getPredicates() {
    return predicates;
  }

  public LoadContext getGraphContext() {
    return request.getGraphContext();
  }

  public SpiOrmQueryRequest<?> getQueryRequest() {
    return request;
  }

  public void cancel() {
    synchronized (this) {
      this.cancelled = true;
      if (pstmt != null) {
        try {
          pstmt.cancel();
        } catch (SQLException e) {
          String msg = "Error cancelling query";
          throw new PersistenceException(msg, e);
        }
      }
    }
  }

  public boolean prepareBindExecuteQuery() throws SQLException {

    synchronized (this) {
      if (cancelled || query.isCancelled()) {
        // cancelled before we started
        cancelled = true;
        return false;
      }

      startNano = System.nanoTime();

      // prepare
      SpiTransaction t = request.getTransaction();
      Connection conn = t.getInternalConnection();
      pstmt = conn.prepareStatement(sql);

      if (query.getTimeout() > 0) {
        pstmt.setQueryTimeout(query.getTimeout());
      }
      if (query.getBufferFetchSizeHint() > 0) {
        pstmt.setFetchSize(query.getBufferFetchSizeHint());
      }

      DataBind dataBind = new DataBind(pstmt);

      // bind keys for encrypted properties
      queryPlan.bindEncryptedProperties(dataBind);

      bindLog = predicates.bind(dataBind);

      // executeQuery
      ResultSet rset = pstmt.executeQuery();
      dataReader = queryPlan.createDataReader(rset);

      return true;
    }
  }

  /**
   * Close the resources.
   * <p>
   * The jdbc resultSet and statement need to be closed. Its important that this
   * method is called.
   * </p>
   */
  public void close() {
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

  /**
   * Return the persistence context.
   */
  public PersistenceContext getPersistenceContext() {
    return persistenceContext;
  }

  public void setLoadedBean(Object bean, Object id) {
    if (id != null && id.equals(loadedBeanId)) {
      // master/detail loading with master bean
      // unchanged. NB Using id to avoid any issue
      // with equals not being implemented

    } else {
      if (manyIncluded) {
        if (rowCount > 1) {
          loadedBeanChanged = true;
        }
        this.prevLoadedBean = loadedBean;
        this.loadedBeanId = id;
      }
      this.loadedBean = bean;
    }
  }

  public void setLoadedManyBean(Object manyValue) {
    this.loadedManyBean = manyValue;
  }

  /**
   * Return the last read bean.
   */
  @SuppressWarnings("unchecked")
  public T getLoadedBean() {
    if (manyIncluded) {
      if (prevDetailCollection instanceof BeanCollection<?>) {
        ((BeanCollection<?>) prevDetailCollection).setModifyListening(manyProperty
            .getModifyListenMode());

      } else if (currentDetailCollection instanceof BeanCollection<?>) {
        ((BeanCollection<?>) currentDetailCollection).setModifyListening(manyProperty
            .getModifyListenMode());
      }
    }

    if (prevLoadedBean != null) {
      return (T) prevLoadedBean;
    } else {
      return (T) loadedBean;
    }
  }

  private boolean hasMoreRows() throws SQLException {
    synchronized (this) {
      if (cancelled) {
        return false;
      }
      return dataReader.next();
    }
  }

  /**
   * Read a row from the result set returning a bean.
   * <p>
   * If the query includes a many then the first object in the returned array is
   * the one/master and the second the many/detail.
   * </p>
   */
  private boolean readRow() throws SQLException {

    synchronized (this) {
      if (cancelled) {
        return false;
      }

      if (!dataReader.next()) {
        return false;
      }

      rowCount++;
      dataReader.resetColumnPosition();

      if (rowNumberIncluded) {
        // row_number() column used for limit features
        dataReader.incrementPos(1);
      }

      rootNode.load(this, null);

      return true;
    }
  }

  public int getQueryExecutionTimeMicros() {
    return executionTimeMicros;
  }

  public boolean readBean() throws SQLException {

    boolean result = readBeanInternal(true);

    updateExecutionStatistics();

    return result;
  }

  private boolean readBeanInternal(boolean inForeground) throws SQLException {

    if (loadedBeanCount >= maxRowsLimit) {
      collection.setHasMoreRows(hasMoreRows());
      return false;
    }

    if (inForeground && loadedBeanCount >= backgroundFetchAfter) {
      hasHitBackgroundFetchAfter = true;
      collection.setFinishedFetch(false);
      return false;
    }

    if (!manyIncluded) {
      // simple query... no details...
      return readRow();
    }

    if (noMoreRows) {
      return false;
    }

    if (rowCount == 0) {
      if (!readRow()) {
        // no rows at all...
        return false;
      } else {
        createNewDetailCollection();
      }
    }

    if (readIntoCurrentDetailCollection()) {
      createNewDetailCollection();
      // return prevLoadedBean
      return true;

    } else {
      // return loadedBean
      prevDetailCollection = null;
      prevLoadedBean = null;
      noMoreRows = true;
      return true;
    }
  }

  private boolean readIntoCurrentDetailCollection() throws SQLException {
    while (readRow()) {
      if (loadedBeanChanged) {
        loadedBeanChanged = false;
        return true;
      } else {
        addToCurrentDetailCollection();
      }
    }
    return false;
  }

  private BeanCollectionAdd currentDetailAdd;

  private void createNewDetailCollection() {
    prevDetailCollection = currentDetailCollection;
    if (queryMode.equals(Mode.LAZYLOAD_MANY)) {
      // just populate the current collection
      currentDetailCollection = manyPropertyEl.elGetValue(loadedBean);
    } else {
      // create a new collection to populate and assign to the bean
      currentDetailCollection = manyProperty.createEmpty(request.isVanillaMode());
      manyPropertyEl.elSetValue(loadedBean, currentDetailCollection, false, false);
    }

    if (filterMany != null && !request.isVanillaMode()) {
      // remember the for use with a refresh
      ((BeanCollection<?>) currentDetailCollection).setFilterMany(filterMany);
    }

    // the manyKey is always null for this case, just using default mapKey on
    // the property
    currentDetailAdd = manyProperty.getBeanCollectionAdd(currentDetailCollection, null);
    addToCurrentDetailCollection();
  }

  private void addToCurrentDetailCollection() {
    if (loadedManyBean != null) {
      currentDetailAdd.addBean(loadedManyBean);
    }
  }

  public BeanCollection<T> continueFetchingInBackground() throws SQLException {
    readTheRows(false);
    collection.setFinishedFetch(true);
    return collection;
  }

  public BeanCollection<T> readCollection() throws SQLException {

    readTheRows(true);

    updateExecutionStatistics();

    return collection;
  }

  protected void updateExecutionStatistics() {
    try {
      long exeNano = System.nanoTime() - startNano;
      executionTimeMicros = (int) exeNano / 1000;

      if (autoFetchProfiling) {
        autoFetchManager
            .collectQueryInfo(autoFetchParentNode, loadedBeanCount, executionTimeMicros);
      }
      queryPlan.executionTime(loadedBeanCount, executionTimeMicros);

    } catch (Exception e) {
      logger.error(null, e);
    }
  }

  public QueryIterator<T> readIterate(int bufferSize, OrmQueryRequest<T> request) {

    if (bufferSize > 0) {
      return new CQueryIteratorWithBuffer<T>(this, request, bufferSize);

    } else {
      return new CQueryIteratorSimple<T>(this, request);
    }
  }

  private void readTheRows(boolean inForeground) throws SQLException {
    while (hasNextBean(inForeground)) {
      if (queryListener != null) {
        queryListener.process(getLoadedBean());

      } else {
        // add to the list/set/map
        help.add(collection, getLoadedBean());
      }
    }
  }

  protected boolean hasNextBean(boolean inForeground) throws SQLException {

    if (!readBeanInternal(inForeground)) {
      return false;

    } else {
      loadedBeanCount++;
      return true;
    }
  }

  public String getLoadedRowDetail() {
    if (!manyIncluded) {
      return String.valueOf(rowCount);
    } else {
      return loadedBeanCount + ":" + rowCount;
    }
  }

  public void register(String path, EntityBeanIntercept ebi) {

    path = getPath(path);
    request.getGraphContext().register(path, ebi);
  }

  public void register(String path, BeanCollection<?> bc) {

    path = getPath(path);
    request.getGraphContext().register(path, bc);
  }

  public boolean useBackgroundToContinueFetch() {
    return hasHitBackgroundFetchAfter;
  }

  /**
   * Return the query name.
   */
  public String getName() {
    return query.getName();
  }

  /**
   * Return true if this is a raw sql query as opposed to Ebean generated sql.
   */
  public boolean isRawSql() {
    return rawSql;
  }

  /**
   * Return the where predicate for display in the transaction log.
   */
  public String getLogWhereSql() {
    return logWhereSql;
  }

  /**
   * Return the property that is associated with the many. There can only be one
   * per SqlSelect. This can be null.
   */
  public BeanPropertyAssocMany<?> getManyProperty() {
    return manyProperty;
  }

  /**
   * Get the summary of the sql.
   */
  public String getSummary() {
    return sqlTree.getSummary();
  }

  /**
   * Return the SqlSelectChain. This is the flattened structure that represents
   * this query.
   */
  public SqlTree getSqlTree() {
    return sqlTree;
  }

  public String getBindLog() {
    return bindLog;
  }

  public SpiTransaction getTransaction() {
    return request.getTransaction();
  }

  public String getBeanType() {
    return desc.getFullName();
  }

  /**
   * Return the short bean name.
   */
  public String getBeanName() {
    return desc.getName();
  }

  /**
   * Return the generated sql.
   */
  public String getGeneratedSql() {
    return sql;
  }

  /**
   * Create a PersistenceException including interesting information like the
   * bindLog and sql used.
   */
  public PersistenceException createPersistenceException(SQLException e) {

    return createPersistenceException(e, getTransaction(), bindLog, sql);
  }

  /**
   * Create a PersistenceException including interesting information like the
   * bindLog and sql used.
   */
  public static PersistenceException createPersistenceException(SQLException e, SpiTransaction t,
      String bindLog, String sql) {

    if (t.isLogSummary()) {
      // log the error to the transaction log
      String errMsg = StringHelper.replaceStringMulti(e.getMessage(), new String[] { "\r", "\n" },
          "\\n ");
      String msg = "ERROR executing query:   bindLog[" + bindLog + "] error[" + errMsg + "]";
      t.logSummary(msg);
    }

    // ensure 'rollback' is logged if queryOnly transaction
    t.getConnection();

    // build a decent error message for the exception
    String m = Message.msg("fetch.sqlerror", e.getMessage(), bindLog, sql);
    return new PersistenceException(m, e);
  }

  /**
   * Should we create profileNodes for beans created in this query.
   * <p>
   * This is true for all queries except lazy load bean queries.
   * </p>
   */
  public boolean isAutoFetchProfiling() {
    // need query.isProfiling() because we just take the data
    // from the lazy loaded or refreshed beans and put it into the already
    // existing beans which are already collecting usage information
    return autoFetchProfiling && query.isUsageProfiling();
  }

  private String getPath(String propertyName) {

    if (currentPrefix == null) {
      return propertyName;
    } else if (propertyName == null) {
      return currentPrefix;
    }

    String path = currentPathMap.get(propertyName);
    if (path != null) {
      return path;
    } else {
      return currentPrefix + "." + propertyName;
    }
  }

  public void profileBean(EntityBeanIntercept ebi, String prefix) {

    ObjectGraphNode node = request.getGraphContext().getObjectGraphNode(prefix);

    ebi.setNodeUsageCollector(new NodeUsageCollector(node, autoFetchManagerRef));
  }

  public void setCurrentPrefix(String currentPrefix, Map<String, String> currentPathMap) {
    this.currentPrefix = currentPrefix;
    this.currentPathMap = currentPathMap;
  }

}
