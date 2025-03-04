package io.ebeaninternal.server.query;

import io.ebean.CancelableQuery;
import io.ebean.QueryIterator;
import io.ebean.Version;
import io.ebean.bean.*;
import io.ebean.core.type.DataReader;
import io.ebean.event.readaudit.ReadEvent;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiQuery.Mode;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.autotune.ProfilingListener;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.core.SpiOrmQueryRequest;
import io.ebeaninternal.server.deploy.*;

import jakarta.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.ERROR;

/**
 * An object that represents a SqlSelect statement.
 * <p>
 * The SqlSelect is based on a tree (Object Graph). The tree is traversed to see
 * what parts are included in the tree according to the value of
 * find.getInclude();
 * <p>
 * The tree structure is flattened into a SqlSelectChain. The SqlSelectChain is
 * the key object used in reading the flat resultSet back into Objects.
 */
public final class CQuery<T> implements DbReadContext, CancelableQuery, SpiProfileTransactionEvent {

  private static final CQueryCollectionAddNoop NOOP_ADD = new CQueryCollectionAddNoop();

  private final ReentrantLock lock = new ReentrantLock();

  private final boolean loadContextBean;
  private final boolean unmodifiable;

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
   * The 'master' bean just loaded.
   */
  private EntityBean nextBean;

  /**
   * Holds the previous loaded bean.
   */
  private EntityBean currentBean;

  /**
   * Caches hasNext for consecutive hasNext() calls.
   */
  private boolean hasNextCache;

  private final BeanPropertyAssocMany<?> lazyLoadManyProperty;

  private Object lazyLoadParentId;

  private EntityBean lazyLoadParentBean;

  /**
   * The 'master' collection being populated.
   */
  private final BeanCollection<T> collection;
  /**
   * The help for the 'master' collection.
   */
  private final CQueryCollectionAdd help;

  /**
   * The overall find request wrapper object.
   */
  private final OrmQueryRequest<T> request;

  private final BeanDescriptor<T> desc;

  private final SpiQuery<T> query;

  private final boolean disableLazyLoading;

  private Map<String, String> currentPathMap;

  private String currentPrefix;

  /**
   * Where clause predicates.
   */
  private final CQueryPredicates predicates;

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
   * Tree that knows how to build the master and detail beans from the
   * resultSet.
   */
  private final SqlTreeRoot rootNode;

  /**
   * For master detail query.
   */
  private final STreePropertyAssocMany manyProperty;

  private DataReader dataReader;

  /**
   * The statement used to create the resultSet.
   */
  private PreparedStatement pstmt;

  private String bindLog;

  private final CQueryPlan queryPlan;

  private final Mode queryMode;

  private final boolean autoTuneProfiling;

  private final ObjectGraphNode objectGraphNode;

  private final ProfilingListener profilingListener;

  private final Boolean readOnly;

  private long profileOffset;
  private long startNano;

  private long executionTimeMicros;

  /**
   * Flag set when read auditing.
   */
  private final boolean audit;

  /**
   * Flag set when findIterate is being read audited meaning we log in batches.
   */
  private boolean auditFindIterate;

  /**
   * A buffer of Ids collected for findIterate auditing.
   */
  private List<Object> auditIds;

  /**
   * Create the Sql select based on the request.
   */
  @SuppressWarnings("unchecked")
  public CQuery(OrmQueryRequest<T> request, CQueryPredicates predicates, CQueryPlan queryPlan) {
    this.request = request;
    this.audit = request.isAuditReads();
    this.queryPlan = queryPlan;
    this.query = request.query();
    this.queryMode = query.mode();
    this.loadContextBean = queryMode.isLoadContextBean() || query.getForUpdateLockType() != null;
    this.lazyLoadManyProperty = query.lazyLoadMany();
    this.readOnly = query.isReadOnly();
    this.disableLazyLoading = query.isDisableLazyLoading();
    this.objectGraphNode = query.parentNode();
    this.profilingListener = query.profilingListener();
    this.autoTuneProfiling = profilingListener != null;
    // set the generated sql back to the query
    // so its available to the user...
    query.setGeneratedSql(queryPlan.sql());
    SqlTreePlan sqlTree = queryPlan.sqlTree();
    this.rootNode = sqlTree.rootNode();
    this.manyProperty = sqlTree.manyProperty();
    this.sql = queryPlan.sql();
    this.rawSql = queryPlan.isRawSql();
    this.logWhereSql = queryPlan.logWhereSql();
    this.desc = request.descriptor();
    this.predicates = predicates;
    if (lazyLoadManyProperty != null) {
      this.help = NOOP_ADD;
    } else {
      this.help = createHelp(request);
    }
    this.unmodifiable = request.query().isUnmodifiable();
    this.collection = (help != null ? help.createEmptyNoParent() : null);
    if (collection != null && Boolean.TRUE.equals(query.isReadOnly())) {
      collection.setReadOnly(true);
    }
  }

  private CQueryCollectionAdd<T> createHelp(OrmQueryRequest<T> request) {
    if (request.isFindById()) {
      return null;
    } else {
      SpiQuery.Type manyType = request.query().type();
      if (manyType == null) {
        // subQuery compiled for InQueryExpression
        return null;
      }
      return BeanCollectionHelpFactory.create(manyType, request);
    }
  }

  @Override
  public boolean isLoadContextBean() {
    return loadContextBean;
  }

  @Override
  public boolean isDraftQuery() {
    return query.isAsDraft();
  }

  @Override
  public boolean isDisableLazyLoading() {
    return disableLazyLoading;
  }

  @Override
  public Boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public void propagateState(Object e) {
    if (Boolean.TRUE.equals(readOnly)) {
      if (e instanceof EntityBean) {
        ((EntityBean) e)._ebean_getIntercept().setReadOnly(true);
      }
    }
  }

  @Override
  public DataReader dataReader() {
    return dataReader;
  }

  @Override
  public Mode queryMode() {
    return queryMode;
  }

  public CQueryPredicates predicates() {
    return predicates;
  }

  SpiOrmQueryRequest<?> request() {
    return request;
  }

  @Override
  public void cancel() {
    lock.lock();
    try {
      JdbcClose.cancel(pstmt);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Prepare bind and execute query with Forward only hints.
   */
  boolean prepareBindExecuteQueryForwardOnly(boolean dbPlatformForwardOnlyHint) throws SQLException {
    return prepareBindExecuteQueryWithOption(dbPlatformForwardOnlyHint);
  }

  /**
   * Prepare bind and execute the query normally.
   */
  boolean prepareBindExecuteQuery() throws SQLException {
    return prepareBindExecuteQueryWithOption(false);
  }

  private boolean prepareBindExecuteQueryWithOption(boolean forwardOnlyHint) throws SQLException {
    ResultSet resultSet = prepareResultSet(forwardOnlyHint);
    if (resultSet == null) {
      return false;
    }
    dataReader = queryPlan.createDataReader(resultSet);
    return true;
  }

  ResultSet prepareResultSet(boolean forwardOnlyHint) throws SQLException {
    lock.lock();
    try {
      // cancelled before we started
      query.checkCancelled();
      startNano = System.nanoTime();
      SpiTransaction t = request.transaction();
      profileOffset = t.profileOffset();
      if (query.isRawSql()) {
        ResultSet suppliedResultSet = query.rawSql().getResultSet();
        if (suppliedResultSet != null) {
          // this is a user supplied ResultSet so use that
          bindLog = "";
          return suppliedResultSet;
        }
      }

      Connection conn = t.internalConnection();
      if (forwardOnlyHint) {
        // Use forward only hints for large resultSet processing (Issue 56, MySql specific)
        pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        pstmt.setFetchSize(request.forwardOnlyFetchSize());
      } else {
        pstmt = conn.prepareStatement(sql);
      }
      if (query.timeout() > 0) {
        pstmt.setQueryTimeout(query.timeout());
      }
      if (query.bufferFetchSizeHint() > 0) {
        pstmt.setFetchSize(query.bufferFetchSizeHint());
      }
      bindLog = predicates.bind(queryPlan.bindEncryptedProperties(pstmt, conn));
    } finally {
      lock.unlock();
    }
    ResultSet ret = pstmt.executeQuery();
    query.checkCancelled();
    return ret;
  }

  /**
   * Close the resources.
   * <p>
   * The JDBC resultSet and statement need to be closed. Its important that this method is called.
   */
  public void close() {
    try {
      if (auditFindIterate && auditIds != null && !auditIds.isEmpty()) {
        auditIterateLogMessage();
      }
    } catch (Throwable e) {
      CoreLog.log.log(ERROR, "Error logging read audit logs", e);
    }
    try {
      if (dataReader != null) {
        dataReader.close();
        dataReader = null;
      }
    } catch (SQLException e) {
      CoreLog.log.log(ERROR, "Error closing dataReader", e);
    }
    JdbcClose.close(pstmt);
    pstmt = null;
  }

  /**
   * Return the persistence context.
   */
  @Override
  public PersistenceContext persistenceContext() {
    return request.persistenceContext();
  }

  @Override
  public void setLazyLoadedChildBean(EntityBean bean, Object lazyLoadParentId) {
    if (lazyLoadParentId != null) {
      if (!lazyLoadParentId.equals(this.lazyLoadParentId)) {
        // get the appropriate parent bean from the persistence context
        this.lazyLoadParentBean = (EntityBean) lazyLoadManyProperty.descriptor().contextGet(persistenceContext(), lazyLoadParentId);
        this.lazyLoadParentId = lazyLoadParentId;
      }
      // add the loadedBean to the appropriate collection of lazyLoadParentBean
      if (lazyLoadParentBean != null) {
        // Note: parentBean can be null, when GC ran between construction of query and building the retrieved bean
        lazyLoadManyProperty.addBeanToCollectionWithCreate(lazyLoadParentBean, bean, true);
      }
    }
  }

  /**
   * Read a row from the result set returning a bean.
   * <p>
   * If the query includes a many then the first object in the returned array is
   * the one/master and the second the many/detail.
   */
  private boolean readNextBean() throws SQLException {
    if (!moveToNextRow()) {
      if (currentBean == null) {
        nextBean = null;
        return false;
      } else {
        // the last bean
        nextBean = currentBean;
        loadedBeanCount++;
        return true;
      }
    }

    loadedBeanCount++;
    if (manyProperty == null) {
      // only single resultSet row required to build object so we are done
      // read a single resultSet row into single bean
      nextBean = rootNode.load(this);
      return true;
    }

    if (nextBean == null) {
      // very first read
      nextBean = rootNode.load(this);
    } else {
      // nextBean set to previously read currentBean
      nextBean = currentBean;
      // check the current row we have just moved to
      if (checkForDifferentBean()) {
        return true;
      }
    }
    readUntilDifferentBeanStarted();
    return true;
  }

  /**
   * Read resultSet rows until we hit the end or get a different bean.
   */
  private void readUntilDifferentBeanStarted() throws SQLException {
    while (moveToNextRow()) {
      if (checkForDifferentBean()) return;
    }
  }

  /**
   * Read the currentBean from the row data returning true if the bean
   * is different to the nextBean (false if we need to read more rows).
   */
  private boolean checkForDifferentBean() throws SQLException {
    currentBean = rootNode.load(this);
    return currentBean != nextBean;
  }

  /**
   * Return true if we can move to the next resultSet row.
   */
  private boolean moveToNextRow() throws SQLException {
    if (!dataReader.next()) {
      noMoreRows = true;
      return false;
    }
    rowCount++;
    return true;
  }

  long queryExecutionTimeMicros() {
    return executionTimeMicros;
  }

  boolean readBean() throws SQLException {
    boolean result = hasNext();
    updateExecutionStatistics();
    return result;
  }

  EntityBean nextBean() {
    EntityBean bean = next();
    if (unmodifiable) {
      request.unmodifiableFreeze(bean);
    }
    return bean;
  }

  EntityBean next() {
    if (audit) {
      auditNextBean();
    }
    hasNextCache = false;
    if (nextBean == null) {
      throw new NoSuchElementException();
    }
    return nextBean;
  }

  boolean hasNext() throws SQLException {
    lock.lock();
    try {
      query.checkCancelled();
      if (noMoreRows) {
        return false;
      }
      if (hasNextCache) {
        return true;
      }
      hasNextCache = readNextBean();
      return hasNextCache;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Read version beans and their effective dates.
   */
  List<Version<T>> readVersions() throws SQLException {
    List<Version<T>> versionList = new ArrayList<>();
    Version<T> version;
    while ((version = readNextVersion()) != null) {
      versionList.add(version);
    }
    updateExecutionStatistics();
    return versionList;
  }

  private Version<T> readNextVersion() throws SQLException {
    if (moveToNextRow()) {
      return rootNode.loadVersion(this);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  BeanCollection<T> readCollection() throws SQLException {
    while (hasNext()) {
      help.add(collection, next(), false);
    }
    updateExecutionStatistics();
    return collection;
  }

  /**
   * Update execution stats and check for slow query.
   */
  private void updateExecutionStatistics() {
    updateStatistics();
    request.slowQueryCheck(executionTimeMicros, rowCount);
  }

  /**
   * Update execution stats but skip slow query check as expected large query.
   */
  void updateExecutionStatisticsIterator() {
    updateStatistics();
  }

  long micros() {
    return (System.nanoTime() - startNano) / 1000L;
  }

  private void updateStatistics() {
    try {
      executionTimeMicros = micros();
      if (autoTuneProfiling) {
        profilingListener.collectQueryInfo(objectGraphNode, loadedBeanCount, executionTimeMicros);
      }
      if (queryPlan.executionTime(executionTimeMicros)) {
        queryPlan.captureBindForQueryPlan(predicates, executionTimeMicros);
      }
      transaction().profileEvent(this);
    } catch (Exception e) {
      CoreLog.log.log(ERROR, "Error updating execution statistics", e);
    }
  }

  @Override
  public void profile() {
    transaction()
      .profileStream()
      .addQueryEvent(query.profileEventId(), profileOffset, desc.name(), loadedBeanCount, query.profileId());
  }

  QueryIterator<T> readIterate(int bufferSize, OrmQueryRequest<T> request) {
    if (bufferSize == 1) {
      return new CQueryIteratorSimple<>(this, request);
    } else {
      return new CQueryIteratorWithBuffer<>(this, request, bufferSize);
    }
  }

  String loadedRowDetail() {
    if (manyProperty == null) {
      return String.valueOf(rowCount);
    } else {
      return loadedBeanCount + ":" + rowCount;
    }
  }

  @Override
  public void registerBeanInherit(BeanPropertyAssocOne<?> property, EntityBeanIntercept ebi) {
    request.loadContext().register(path(property.name()), ebi, property);
  }

  @Override
  public void register(String path, EntityBeanIntercept ebi) {
    request.loadContext().register(path(path), ebi);
  }

  @Override
  public void register(BeanPropertyAssocMany<?> many, BeanCollection<?> bc) {
    request.loadContext().register(path(many.name()), many, bc);
  }

  @Override
  public boolean includeSecondary(BeanPropertyAssocMany<?> many) {
    return request.loadContext().includeSecondary(many);
  }

  /**
   * Return true if this is a raw sql query as opposed to Ebean generated sql.
   */
  @Override
  public boolean isRawSql() {
    return rawSql;
  }

  /**
   * Return the where predicate for display in the transaction log.
   */
  String logWhereSql() {
    return logWhereSql;
  }

  public String bindLog() {
    return bindLog;
  }

  public SpiTransaction transaction() {
    return request.transaction();
  }

  /**
   * Return the short bean name.
   */
  String beanName() {
    return desc.name();
  }

  /**
   * Return the generated sql.
   */
  public String generatedSql() {
    return sql;
  }

  /**
   * Create a PersistenceException including interesting information like the bindLog and sql used.
   */
  PersistenceException createPersistenceException(SQLException e) {
    return request.translate(bindLog, sql, e);
  }

  /**
   * Should we create profileNodes for beans created in this query.
   * <p>
   * This is true for all queries except lazy load bean queries.
   */
  @Override
  public boolean isAutoTuneProfiling() {
    // need query.isProfiling() because we just take the data
    // from the lazy loaded or refreshed beans and put it into the already
    // existing beans which are already collecting usage information
    return autoTuneProfiling && query.isUsageProfiling();
  }

  private String path(String propertyName) {
    if (currentPrefix == null) {
      return propertyName;
    } else if (propertyName == null) {
      return currentPrefix;
    }
    String path = currentPathMap.get(propertyName);
    return path != null ? path : currentPrefix + "." + propertyName;
  }

  @Override
  public void profileBean(EntityBeanIntercept ebi, String prefix) {
    ObjectGraphNode node = request.loadContext().objectGraphNode(prefix);
    ebi.setNodeUsageCollector(new NodeUsageCollector(node, profilingListener));
  }

  @Override
  public void setCurrentPrefix(String currentPrefix, Map<String, String> currentPathMap) {
    this.currentPrefix = currentPrefix;
    this.currentPathMap = currentPathMap;
  }

  /**
   * A find bean query with read auditing so build and log the ReadEvent.
   */
  void auditFind(EntityBean bean) {
    if (bean != null) {
      // only audit when a bean was actually found
      desc.readAuditBean(queryPlan.auditQueryKey(), bindLog, bean);
    }
  }

  /**
   * a find many query with read auditing so build the ReadEvent and log it.
   */
  void auditFindMany() {
    if (auditIds != null && !auditIds.isEmpty()) {
      // get the id values of the underlying collection
      ReadEvent futureReadEvent = query.futureFetchAudit();
      if (futureReadEvent == null) {
        // normal query execution
        desc.readAuditMany(queryPlan.auditQueryKey(), bindLog, auditIds);
      } else {
        // this query was executed via findFutureList() and the prepare()
        // has already been called so set the details and log
        futureReadEvent.setQueryKey(queryPlan.auditQueryKey());
        futureReadEvent.setBindLog(bindLog);
        futureReadEvent.setIds(auditIds);
        desc.readAuditFutureMany(futureReadEvent);
      }
    }
  }

  /**
   * Indicate that read auditing is occurring on this findIterate query.
   */
  void auditFindIterate() {
    auditFindIterate = true;
  }

  /**
   * Send the current buffer of findIterate collected ids to the audit log.
   */
  private void auditIterateLogMessage() {
    desc.readAuditMany(queryPlan.auditQueryKey(), bindLog, auditIds);
    // create a new list on demand with the next bean/id
    auditIds = null;
  }

  /**
   * Add the id to the audit id buffer and flush if needed in batches of 100.
   */
  private void auditNextBean() {
    if (auditIds == null) {
      auditIds = new ArrayList<>(100);
    }
    auditIds.add(desc.idForJson(nextBean));
    if (auditFindIterate && auditIds.size() >= 100) {
      auditIterateLogMessage();
    }
  }

  /**
   * Return the underlying PreparedStatement.
   */
  PreparedStatement pstmt() {
    return pstmt;
  }

  @Override
  public void handleLoadError(String fullName, Exception e) {
    query.handleLoadError(fullName, e);
  }

  public Set<String> dependentTables() {
    return queryPlan.dependentTables();
  }
}
