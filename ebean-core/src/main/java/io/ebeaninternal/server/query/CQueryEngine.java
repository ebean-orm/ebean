package io.ebeaninternal.server.query;

import io.ebean.QueryIterator;
import io.ebean.ValuePair;
import io.ebean.Version;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.util.JdbcClose;
import io.ebean.util.StringHelper;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.DiffHelp;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.core.SpiResultSet;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.util.Str;
import io.ebeaninternal.server.persist.Binder;
import org.slf4j.Logger;

import javax.persistence.PersistenceException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the Object Relational fetching.
 */
public final class CQueryEngine {

  private static final Logger log = CoreLog.log;

  private static final String T0 = "t0";

  private final int defaultFetchSizeFindList;

  private final int defaultFetchSizeFindEach;

  private final boolean forwardOnlyHintOnFindIterate;

  private final CQueryBuilder queryBuilder;

  private final CQueryHistorySupport historySupport;

  private final DatabasePlatform dbPlatform;

  public CQueryEngine(DatabaseConfig config, DatabasePlatform dbPlatform, Binder binder, Map<String, String> asOfTableMapping, Map<String, String> draftTableMap) {
    this.dbPlatform = dbPlatform;
    this.defaultFetchSizeFindEach = config.getJdbcFetchSizeFindEach();
    this.defaultFetchSizeFindList = config.getJdbcFetchSizeFindList();
    this.forwardOnlyHintOnFindIterate = dbPlatform.isForwardOnlyHintOnFindIterate();
    this.historySupport = new CQueryHistorySupport(dbPlatform.getHistorySupport(), asOfTableMapping, config.getAsOfSysPeriod());
    this.queryBuilder = new CQueryBuilder(dbPlatform, binder, historySupport, new CQueryDraftSupport(draftTableMap));
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
        request.logSql(Str.add(query.getGeneratedSql(), "; --bind(", query.getBindLog(), ") --micros(", query.micros() + ") --rows(", rows + ")"));
      }
      return rows;
    } catch (SQLException e) {
      throw translate(request, query.getBindLog(), query.getGeneratedSql(), e);
    }
  }

  /**
   * Build and execute the findSingleAttributeList query.
   */
  public <A> List<A> findSingleAttributeList(OrmQueryRequest<?> request) {
    CQueryFetchSingleAttribute rcQuery = queryBuilder.buildFetchAttributeQuery(request);
    request.setCancelableQuery(rcQuery);
    return findAttributeList(request, rcQuery);
  }

  @SuppressWarnings("unchecked")
  private <A> List<A> findAttributeList(OrmQueryRequest<?> request, CQueryFetchSingleAttribute rcQuery) {
    try {
      List<A> list = (List<A>) rcQuery.findList();
      if (request.logSql()) {
        logGeneratedSql(request, rcQuery.getGeneratedSql(), rcQuery.getBindLog(), rcQuery.micros());
      }
      if (request.logSummary()) {
        request.transaction().logSummary(rcQuery.getSummary());
      }
      if (request.isQueryCachePut()) {
        request.addDependentTables(rcQuery.getDependentTables());
        list = Collections.unmodifiableList(list);
        request.putToQueryCache(list);
        if (Boolean.FALSE.equals(request.query().isReadOnly())) {
          list = new ArrayList<>(list);
        }
      }
      return list;
    } catch (SQLException e) {
      throw translate(request, rcQuery.getBindLog(), rcQuery.getGeneratedSql(), e);
    }
  }

  /**
   * Translate the SQLException into a PersistenceException.
   */
  <T> PersistenceException translate(OrmQueryRequest<T> request, String bindLog, String sql, SQLException e) {
    SpiTransaction t = request.transaction();
    if (t.isLogSummary()) {
      // log the error to the transaction log
      String msg = "ERROR executing query, bindLog[" + bindLog + "] error[" + StringHelper.removeNewLines(e.getMessage()) + "]";
      t.logSummary(msg);
    }
    // ensure 'rollback' is logged if queryOnly transaction
    t.connection();
    // build a decent error message for the exception
    String m = "Query threw SQLException:" + e.getMessage() + " Bind values:[" + bindLog + "] Query was:" + sql;
    return dbPlatform.translate(m, e);
  }

  /**
   * Build and execute the find Id's query.
   */
  public <A> List<A> findIds(OrmQueryRequest<?> request) {
    CQueryFetchSingleAttribute rcQuery = queryBuilder.buildFetchIdsQuery(request);
    request.setCancelableQuery(rcQuery);
    return findAttributeList(request, rcQuery);
  }

  private <T> void logGeneratedSql(OrmQueryRequest<T> request, String sql, String bindLog, long micros) {
    request.logSql(Str.add(sql, "; --bind(", bindLog, ") --micros(", micros + ")"));
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
        logGeneratedSql(request, rcQuery.getGeneratedSql(), rcQuery.getBindLog(), rcQuery.micros());
      }
      if (request.logSummary()) {
        request.transaction().logSummary(rcQuery.getSummary());
      }
      if (request.query().isFutureFetch()) {
        request.transaction().end();
      }
      if (request.isQueryCachePut()) {
        request.addDependentTables(rcQuery.getDependentTables());
        request.putToQueryCache(count);
      }
      return count;
    } catch (SQLException e) {
      throw translate(request, rcQuery.getBindLog(), rcQuery.getGeneratedSql(), e);
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
      if (!cquery.prepareBindExecuteQueryForwardOnly(forwardOnlyHintOnFindIterate)) {
        // query has been cancelled already
        log.trace("Future fetch already cancelled");
        return null;
      }
      if (request.logSql()) {
        logSql(cquery);
      }
      // first check batch sizes set on query joins
      int iterateBufferSize = request.secondaryQueriesMinBatchSize();
      if (iterateBufferSize < 1) {
        // not set on query joins so check if batch size set on query itself
        int queryBatch = request.query().getLazyLoadBatchSize();
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
      if (request.isAuditReads()) {
        // indicates we need to audit as the iterator progresses
        cquery.auditFindIterate();
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
      query.where().lt(sysPeriodLower, query.getVersionEnd());
      query.where().geOrNull(getSysPeriodUpper(query), query.getVersionStart());
    }

    // order by lower sys period desc
    query.order().desc(sysPeriodLower);
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
      if (request.isAuditReads()) {
        cquery.auditFindMany();
      }
      return versions;

    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
    } finally {
      if (cquery != null) {
        cquery.close();
      }
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
    return historySupport.getSysPeriodLower(query.getAlias(T0));
  }

  private <T> String getSysPeriodUpper(SpiQuery<T> query) {
    return historySupport.getSysPeriodUpper(query.getAlias(T0));
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
      return new SpiResultSet(cquery.getPstmt(), resultSet);

    } catch (SQLException e) {
      JdbcClose.close(cquery.getPstmt());
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
        log.trace("Future fetch already cancelled");
        return null;
      }
      if (request.logSql()) {
        logSql(cquery);
      }
      BeanCollection<T> beanCollection = cquery.readCollection();
      if (request.logSummary()) {
        logFindManySummary(cquery);
      }
      if (request.isAuditReads()) {
        cquery.auditFindMany();
      }
      request.executeSecondaryQueries(false);
      if (request.isQueryCachePut()) {
        request.addDependentTables(cquery.getDependentTables());
      }
      return beanCollection;

    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
    } finally {
      if (cquery != null) {
        cquery.close();
      }
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
      if (request.isAuditReads()) {
        cquery.auditFind(bean);
      }
      request.executeSecondaryQueries(false);
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
    query.getTransaction().logSql(Str.add(query.getGeneratedSql(), "; --bind(", query.getBindLog(), ") --micros(", query.micros() + ")"));
  }

  /**
   * Log the FindById summary to the transaction log.
   */
  private void logFindBeanSummary(CQuery<?> q) {
    SpiQuery<?> query = q.getQueryRequest().query();
    String loadMode = query.getLoadMode();
    String loadDesc = query.getLoadDescription();
    String lazyLoadProp = query.getLazyLoadProperty();
    ObjectGraphNode node = query.getParentNode();
    String originKey;
    if (node == null || node.getOriginQueryPoint() == null) {
      originKey = null;
    } else {
      originKey = node.getOriginQueryPoint().getKey();
    }

    StringBuilder msg = new StringBuilder(200);
    msg.append("FindBean ");
    if (loadMode != null) {
      msg.append("mode[").append(loadMode).append("] ");
    }
    msg.append("type[").append(q.getBeanName()).append("] ");
    if (query.isAutoTuned()) {
      msg.append("tuned[true] ");
    }
    if (query.isAsDraft()) {
      msg.append(" draft[true] ");
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
    msg.append("exeMicros[").append(q.getQueryExecutionTimeMicros());
    msg.append("] rows[").append(q.getLoadedRowDetail());
    msg.append("] bind[").append(q.getBindLog()).append("]");
    q.getTransaction().logSummary(msg.toString());
  }

  /**
   * Log the FindMany to the transaction log.
   */
  private void logFindManySummary(CQuery<?> q) {
    SpiQuery<?> query = q.getQueryRequest().query();
    String loadMode = query.getLoadMode();
    String loadDesc = query.getLoadDescription();
    String lazyLoadProp = query.getLazyLoadProperty();
    ObjectGraphNode node = query.getParentNode();

    String originKey;
    if (node == null || node.getOriginQueryPoint() == null) {
      originKey = null;
    } else {
      originKey = node.getOriginQueryPoint().getKey();
    }

    StringBuilder msg = new StringBuilder(200);
    msg.append("FindMany ");
    if (loadMode != null) {
      msg.append("mode[").append(loadMode).append("] ");
    }
    msg.append("type[").append(q.getBeanName()).append("] ");
    if (query.isAutoTuned()) {
      msg.append("tuned[true] ");
    }
    if (query.isAsDraft()) {
      msg.append(" draft[true] ");
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
    msg.append("exeMicros[").append(q.getQueryExecutionTimeMicros());
    msg.append("] rows[").append(q.getLoadedRowDetail());
    msg.append("] predicates[").append(q.getLogWhereSql());
    msg.append("] bind[").append(q.getBindLog()).append("]");
    q.getTransaction().logSummary(msg.toString());
  }
}
