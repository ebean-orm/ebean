package io.ebeaninternal.server.query;

import io.ebean.QueryIterator;
import io.ebean.ValuePair;
import io.ebean.Version;
import io.ebean.annotation.Platform;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.DatabaseBuilder;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.util.JdbcClose;
import io.ebean.util.StringHelper;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.DiffHelp;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.core.SpiResultSet;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.persist.Binder;

import jakarta.persistence.PersistenceException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Handles the Object Relational fetching.
 */
public final class CQueryEngine {

  private static final String T0 = "t0";

  private final int defaultFetchSizeFindList;
  private final int defaultFetchSizeFindEach;
  private final boolean forwardOnlyHintOnFindIterate;
  private final CQueryBuilder queryBuilder;
  private final CQueryHistorySupport historySupport;
  private final DatabasePlatform dbPlatform;
  private final boolean autoCommitFalseOnFindIterate;

  public CQueryEngine(DatabaseBuilder.Settings config, DatabasePlatform dbPlatform, Binder binder, Map<String, String> asOfTableMapping) {
    this.dbPlatform = dbPlatform;
    this.defaultFetchSizeFindEach = config.getJdbcFetchSizeFindEach();
    this.defaultFetchSizeFindList = config.getJdbcFetchSizeFindList();
    this.forwardOnlyHintOnFindIterate = dbPlatform.forwardOnlyHintOnFindIterate();
    this.autoCommitFalseOnFindIterate = dbPlatform.autoCommitFalseOnFindIterate();
    this.historySupport = new CQueryHistorySupport(dbPlatform.historySupport(), asOfTableMapping, config.getAsOfSysPeriod());
    this.queryBuilder = new CQueryBuilder(config, dbPlatform, binder, historySupport);
  }

  public int forwardOnlyFetchSize() {
    Platform base = dbPlatform.platform().base();
    return Platform.MYSQL == base ? Integer.MIN_VALUE : 1;
  }

  public <T> CQuery<T> buildQuery(OrmQueryRequest<T> request) {
    return queryBuilder.buildQuery(request);
  }

  public <T> int delete(OrmQueryRequest<T> request) {
    CQueryUpdate query = queryBuilder.buildUpdateQuery(true, request);
    request.setCancelableQuery(query);
    return executeUpdate(request, query);
  }

  public <T> int update(OrmQueryRequest<T> request) {
    CQueryUpdate query = queryBuilder.buildUpdateQuery(false, request);
    request.setCancelableQuery(query);
    return executeUpdate(request, query);
  }

  private <T> int executeUpdate(OrmQueryRequest<T> request, CQueryUpdate query) {
    try {
      int rows = query.execute();
      if (request.logSql()) {
        request.logSql("{0}; --bind({1}) --micros({2}) --rows({3})", query.generatedSql(), query.bindLog(), query.micros(), rows);
      }
      if (rows > 0) {
        request.clearContext();
      }
      return rows;
    } catch (SQLException e) {
      throw translate(request, query.bindLog(), query.generatedSql(), e);
    }
  }

  /**
   * Build and execute the findSingleAttributeList query.
   */
  public <A extends Collection<?>> A findSingleAttributeList(OrmQueryRequest<?> request, A collection) {
    CQueryFetchSingleAttribute rcQuery = queryBuilder.buildFetchAttributeQuery(request);
    request.setCancelableQuery(rcQuery);
    return findAttributeCollection(request, rcQuery, collection);
  }

  @SuppressWarnings("unchecked")
  private <A extends Collection<?>> A findAttributeCollection(OrmQueryRequest<?> request, CQueryFetchSingleAttribute rcQuery, A collection) {
    try {
      rcQuery.findCollection(collection);
      if (request.logSql()) {
        logGeneratedSql(request, rcQuery.generatedSql(), rcQuery.bindLog(), rcQuery.micros());
      }
      if (request.logSummary()) {
        request.transaction().logSummary(rcQuery.summary());
      }
      if (request.isQueryCachePut()) {
        request.addDependentTables(rcQuery.dependentTables());
        if (collection instanceof List) {
          collection = (A) Collections.unmodifiableList((List<?>) collection);
          request.putToQueryCache(collection);
        } else if (collection instanceof Set) {
          collection = (A) Collections.unmodifiableSet((Set<?>) collection);
          request.putToQueryCache(collection);
        }
      }
      return collection;
    } catch (SQLException e) {
      throw translate(request, rcQuery.bindLog(), rcQuery.generatedSql(), e);
    }
  }

  /**
   * Translate the SQLException into a PersistenceException.
   */
  <T> PersistenceException translate(OrmQueryRequest<T> request, String bindLog, String sql, SQLException e) {
    SpiTransaction t = request.transaction();
    if (t.isLogSummary()) {
      // log the error to the transaction log
      t.logSummary("ERROR executing query, bindLog[{0}] error:{1}", bindLog, StringHelper.removeNewLines(e.getMessage()));
    }
    // ensure 'rollback' is logged if queryOnly transaction
    t.connection();
    // build a decent error message for the exception
    return dbPlatform.translate("Query threw SQLException:" + e.getMessage() + " Bind values:[" + bindLog + "] Query was:" + sql, e);
  }

  /**
   * Build and execute the find Id's query.
   */
  public <A> List<A> findIds(OrmQueryRequest<?> request) {
    CQueryFetchSingleAttribute rcQuery = queryBuilder.buildFetchIdsQuery(request);
    request.setCancelableQuery(rcQuery);
    return findAttributeCollection(request, rcQuery, new ArrayList<>());
  }

  private <T> void logGeneratedSql(OrmQueryRequest<T> request, String sql, String bindLog, long micros) {
    request.logSql("{0}; --bind({1}) --micros({2})", sql, bindLog, micros);
  }

  /**
   * Build and execute the row count query.
   */
  public <T> int findCount(OrmQueryRequest<T> request) {
    CQueryRowCount rcQuery = queryBuilder.buildRowCountQuery(request);
    request.setCancelableQuery(rcQuery);
    try {
      int count = rcQuery.findCount();
      if (request.logSql()) {
        logGeneratedSql(request, rcQuery.generatedSql(), rcQuery.bindLog(), rcQuery.micros());
      }
      if (request.logSummary()) {
        request.transaction().logSummary(rcQuery.summary());
      }
      if (request.isQueryCachePut()) {
        request.addDependentTables(rcQuery.dependentTables());
        request.putToQueryCache(count);
      }
      return count;
    } catch (SQLException e) {
      throw translate(request, rcQuery.bindLog(), rcQuery.generatedSql(), e);
    }
  }

  /**
   * Read many beans using an iterator (except you need to close() the iterator
   * when you have finished).
   */
  public <T> QueryIterator<T> findIterate(OrmQueryRequest<T> request) {
    CQuery<T> cquery = queryBuilder.buildQuery(request);
    request.setCancelableQuery(cquery);
    try {
      if (defaultFetchSizeFindEach > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindEach);
      }
      if (autoCommitFalseOnFindIterate) {
        request.setAutoCommitOnFindIterate();
      }
      if (!cquery.prepareBindExecuteQueryForwardOnly(forwardOnlyHintOnFindIterate)) {
        // query has been cancelled already
        return null;
      }
      if (request.logSql()) {
        logSql(cquery);
      }
      // first check batch sizes set on query joins
      int iterateBufferSize = request.secondaryQueriesMinBatchSize();
      if (iterateBufferSize < 1) {
        // not set on query joins so check if batch size set on query itself
        int queryBatch = request.query().lazyLoadBatchSize();
        if (queryBatch > 0) {
          iterateBufferSize = queryBatch;
        } else {
          iterateBufferSize = 100;
        }
      }

      QueryIterator<T> readIterate = cquery.readIterate(iterateBufferSize, request);
      if (request.logSummary()) {
        logFindManySummary(cquery);
      }
      return readIterate;

    } catch (SQLException e) {
      try {
        PersistenceException pex = cquery.createPersistenceException(e);
        // create exception before closing connection
        cquery.close();
        throw pex;
      } finally {
        request.rollbackTransIfRequired();
      }
    }
  }

  /**
   * Execute the find versions query returning version beans.
   */
  public <T> List<Version<T>> findVersions(OrmQueryRequest<T> request) {
    SpiQuery<T> query = request.query();
    String sysPeriodLower = getSysPeriodLower(query);
    if (query.isVersionsBetween() && !historySupport.isStandardsBased()) {
      query.where().lt(sysPeriodLower, query.versionEnd());
      query.where().geOrNull(getSysPeriodUpper(query), query.versionStart());
    }

    // order by lower sys period desc
    query.orderBy().desc(sysPeriodLower);
    CQuery<T> cquery = queryBuilder.buildQuery(request);
    request.setCancelableQuery(cquery);
    try {
      cquery.prepareBindExecuteQuery();
      if (request.logSql()) {
        logSql(cquery);
      }
      List<Version<T>> versions = cquery.readVersions();
      // just order in memory rather than use NULLS LAST as that
      // is not universally supported, not expect huge list here
      versions.sort(OrderVersionDesc.INSTANCE);
      deriveVersionDiffs(versions, request);
      if (request.logSummary()) {
        logFindManySummary(cquery);
      }
      return versions;

    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
    } finally {
      cquery.close();
    }
  }

  private <T> void deriveVersionDiffs(List<Version<T>> versions, OrmQueryRequest<T> request) {
    BeanDescriptor<T> descriptor = request.descriptor();
    if (!versions.isEmpty()) {
      Version<T> current = versions.get(0);
      if (versions.size() > 1) {
        for (int i = 1; i < versions.size(); i++) {
          Version<T> next = versions.get(i);
          deriveVersionDiff(current, next, descriptor);
          current = next;
        }
      }
      // put an empty map into the last one
      current.setDiff(new LinkedHashMap<>());
    }
  }

  private <T> void deriveVersionDiff(Version<T> current, Version<T> prior, BeanDescriptor<T> descriptor) {
    Map<String, ValuePair> diff = DiffHelp.diff(current.getBean(), prior.getBean(), descriptor);
    current.setDiff(diff);
  }

  private <T> String getSysPeriodLower(SpiQuery<T> query) {
    return historySupport.sysPeriodLower(query.getAlias(T0));
  }

  private <T> String getSysPeriodUpper(SpiQuery<T> query) {
    return historySupport.sysPeriodUpper(query.getAlias(T0));
  }

  /**
   * Execute returning the ResultSet and PreparedStatement for processing (by DTO query usually).
   */
  public <T> SpiResultSet findResultSet(OrmQueryRequest<T> request) {
    CQuery<T> cquery = queryBuilder.buildQuery(request);
    request.setCancelableQuery(cquery);
    try {
      boolean fwdOnly;
      if (request.isFindIterate()) {
        // findEach ...
        fwdOnly = forwardOnlyHintOnFindIterate;
        if (defaultFetchSizeFindEach > 0) {
          request.setDefaultFetchBuffer(defaultFetchSizeFindEach);
        }
      } else {
        // findList - aggressive fetch
        fwdOnly = false;
        if (defaultFetchSizeFindList > 0) {
          request.setDefaultFetchBuffer(defaultFetchSizeFindList);
        }
      }
      ResultSet resultSet = cquery.prepareResultSet(fwdOnly);
      if (request.logSql()) {
        logSql(cquery);
      }
      return new SpiResultSet(cquery.pstmt(), resultSet);

    } catch (SQLException e) {
      JdbcClose.close(cquery.pstmt());
      throw cquery.createPersistenceException(e);
    }
  }

  /**
   * Find a list/map/set of beans.
   */
  <T> BeanCollection<T> findMany(OrmQueryRequest<T> request) {
    CQuery<T> cquery = queryBuilder.buildQuery(request);
    request.setCancelableQuery(cquery);
    try {
      if (defaultFetchSizeFindList > 0) {
        request.setDefaultFetchBuffer(defaultFetchSizeFindList);
      }
      if (!cquery.prepareBindExecuteQuery()) {
        // query has been cancelled already
        return null;
      }
      if (request.logSql()) {
        logSql(cquery);
      }
      BeanCollection<T> beanCollection = cquery.readCollection();
      if (request.logSummary()) {
        logFindManySummary(cquery);
      }
      request.executeSecondaryQueries(false);
      if (request.isQueryCachePut()) {
        request.addDependentTables(cquery.dependentTables());
      }
      request.unmodifiableFreeze(beanCollection);
      return beanCollection;

    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
    } finally {
      cquery.close();
    }
  }

  /**
   * Find and return a single bean using its unique id.
   */
  @SuppressWarnings("unchecked")
  public <T> T find(OrmQueryRequest<T> request) {
    EntityBean bean = null;
    CQuery<T> cquery = queryBuilder.buildQuery(request);
    request.setCancelableQuery(cquery);
    try {
      cquery.prepareBindExecuteQuery();
      if (request.logSql()) {
        logSql(cquery);
      }
      if (cquery.readBean()) {
        bean = cquery.next();
      }
      if (request.logSummary()) {
        logFindBeanSummary(cquery);
      }
      request.executeSecondaryQueries(false);
      request.unmodifiableFreeze(bean);
      return (T) bean;
    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
    } finally {
      cquery.close();
    }
  }

  /**
   * Log the generated SQL to the transaction log.
   */
  private void logSql(CQuery<?> query) {
    query.transaction().logSql("{0}; --bind({1}) --micros({2})", query.generatedSql(), query.bindLog(), query.micros());
  }

  /**
   * Log the FindById summary to the transaction log.
   */
  private void logFindBeanSummary(CQuery<?> q) {
    SpiQuery<?> query = q.request().query();
    String loadMode = query.loadMode();
    String loadDesc = query.loadDescription();
    String lazyLoadProp = query.lazyLoadProperty();
    ObjectGraphNode node = query.parentNode();
    String originKey;
    if (node == null || node.origin() == null) {
      originKey = null;
    } else {
      originKey = node.origin().key();
    }

    StringBuilder msg = new StringBuilder(200);
    msg.append("FindBean ");
    if (loadMode != null) {
      msg.append("mode[").append(loadMode).append("] ");
    }
    msg.append("type[").append(q.beanName()).append("] ");
    if (query.isAutoTuned()) {
      msg.append("tuned[true] ");
    }
    if (originKey != null) {
      msg.append("origin[").append(originKey).append("] ");
    }
    if (lazyLoadProp != null) {
      msg.append("lazyLoadProp[").append(lazyLoadProp).append("] ");
    }
    if (loadDesc != null) {
      msg.append("load[").append(loadDesc).append("] ");
    }
    msg.append("exeMicros[").append(q.queryExecutionTimeMicros());
    msg.append("] rows[").append(q.loadedRowDetail());
    msg.append("] bind[").append(q.bindLog()).append(']');
    q.transaction().logSummary(msg.toString());
  }

  /**
   * Log the FindMany to the transaction log.
   */
  private void logFindManySummary(CQuery<?> q) {
    SpiQuery<?> query = q.request().query();
    String loadMode = query.loadMode();
    String loadDesc = query.loadDescription();
    String lazyLoadProp = query.lazyLoadProperty();
    ObjectGraphNode node = query.parentNode();

    String originKey;
    if (node == null || node.origin() == null) {
      originKey = null;
    } else {
      originKey = node.origin().key();
    }

    StringBuilder msg = new StringBuilder(200);
    msg.append("FindMany ");
    if (loadMode != null) {
      msg.append("mode[").append(loadMode).append("] ");
    }
    msg.append("type[").append(q.beanName()).append("] ");
    if (query.isAutoTuned()) {
      msg.append("tuned[true] ");
    }
    if (originKey != null) {
      msg.append("origin[").append(originKey).append("] ");
    }
    if (lazyLoadProp != null) {
      msg.append("lazyLoadProp[").append(lazyLoadProp).append("] ");
    }
    if (loadDesc != null) {
      msg.append("load[").append(loadDesc).append("] ");
    }
    msg.append("exeMicros[").append(q.queryExecutionTimeMicros());
    msg.append("] rows[").append(q.loadedRowDetail());
    msg.append("] predicates[").append(q.logWhereSql());
    msg.append("] bind[").append(q.bindLog()).append(']');
    q.transaction().logSummary(msg.toString());
  }
}
