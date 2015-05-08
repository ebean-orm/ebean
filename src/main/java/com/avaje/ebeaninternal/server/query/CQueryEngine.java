package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionTouched;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.api.BeanIdList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.lib.util.Str;
import com.avaje.ebeaninternal.server.persist.Binder;
import com.avaje.ebeaninternal.server.transaction.TransactionManager;

/**
 * Handles the Object Relational fetching.
 */
public class CQueryEngine {

  private static final Logger logger = LoggerFactory.getLogger(CQueryEngine.class);

  private static final int defaultSecondaryQueryBatchSize = 100;

  private final boolean forwardOnlyHintOnFindIterate;

  private final CQueryBuilder queryBuilder;

  public CQueryEngine(DatabasePlatform dbPlatform, Binder binder) {
    this.forwardOnlyHintOnFindIterate = dbPlatform.isForwardOnlyHintOnFindIterate();
    this.queryBuilder = new CQueryBuilder(dbPlatform, binder);
  }

  public <T> CQuery<T> buildQuery(OrmQueryRequest<T> request) {
    return queryBuilder.buildQuery(request);
  }

  /**
   * Build and execute the find Id's query.
   */
  public <T> BeanIdList findIds(OrmQueryRequest<T> request) {

    CQueryFetchIds rcQuery = queryBuilder.buildFetchIdsQuery(request);
    try {

      
      BeanIdList list = rcQuery.findIds();

      if (request.isLogSql()) {
        String logSql = rcQuery.getGeneratedSql();
        if (TransactionManager.SQL_LOGGER.isTraceEnabled()) {
          logSql = Str.add(logSql, "; --bind(", rcQuery.getBindLog(), ")");
        }
        request.logSql(logSql);
      }

      if (request.isLogSummary()) {
        request.getTransaction().logSummary(rcQuery.getSummary());
      }

      if (!list.isFetchingInBackground() && request.getQuery().isFutureFetch()) {
        // end the transaction for futureFindIds (it had it's own one)
        logger.debug("Future findIds completed!");
        request.getTransaction().end();
      }

      return list;

    } catch (SQLException e) {
      throw CQuery.createPersistenceException(e, request.getTransaction(), rcQuery.getBindLog(), rcQuery.getGeneratedSql());
    }
  }

  /**
   * Build and execute the row count query.
   */
  public <T> int findRowCount(OrmQueryRequest<T> request) {

    CQueryRowCount rcQuery = queryBuilder.buildRowCountQuery(request);
    try {
     
      int rowCount = rcQuery.findRowCount();

      if (request.isLogSql()) {
        String logSql = rcQuery.getGeneratedSql();
        if (TransactionManager.SQL_LOGGER.isTraceEnabled()) {
          logSql= Str.add(logSql, "; --bind(", rcQuery.getBindLog(), ")");
        }
        request.logSql(logSql);
      }

      if (request.isLogSummary()) {
        request.getTransaction().logSummary(rcQuery.getSummary());
      }

      if (request.getQuery().isFutureFetch()) {
        logger.debug("Future findRowCount completed!");
        request.getTransaction().end();
      }

      return rowCount;

    } catch (SQLException e) {
      throw CQuery.createPersistenceException(e, request.getTransaction(), rcQuery.getBindLog(), rcQuery.getGeneratedSql());
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

      if (!cquery.prepareBindExecuteQueryForwardOnly(forwardOnlyHintOnFindIterate)) {
        // query has been cancelled already
        logger.trace("Future fetch already cancelled");
        return null;
      }

      if (request.isLogSql()) {
        logSql(cquery);
      }

      // first check batch sizes set on query joins
      int iterateBufferSize = request.getSecondaryQueriesMinBatchSize(defaultSecondaryQueryBatchSize);
      if (iterateBufferSize < 1) {
        // not set on query joins so check if batch size set on query itself
        int queryBatch = request.getQuery().getLazyLoadBatchSize();
        if (queryBatch > 0) {
          iterateBufferSize = queryBatch;
        }
      }

      QueryIterator<T> readIterate = cquery.readIterate(iterateBufferSize, request);

      if (request.isLogSummary()) {
        logFindManySummary(cquery);
      }

      return readIterate;

    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
    }
  }

  /**
   * Find a list/map/set of beans.
   */
  public <T> BeanCollection<T> findMany(OrmQueryRequest<T> request) {

    CQuery<T> cquery = queryBuilder.buildQuery(request);
    request.setCancelableQuery(cquery);

    try {
      if (!cquery.prepareBindExecuteQuery()) {
        // query has been cancelled already
        logger.trace("Future fetch already cancelled");
        return null;
      }
      
      if (request.isLogSql()) {
        logSql(cquery);
      }

      BeanCollection<T> beanCollection = cquery.readCollection();

      BeanCollectionTouched collectionTouched = request.getQuery().getBeanCollectionTouched();
      if (collectionTouched != null) {
        // register a listener that wants to be notified when the
        // bean collection is first used
        beanCollection.setBeanCollectionTouched(collectionTouched);
      }

      if (request.isLogSummary()) {
        logFindManySummary(cquery);
      }

      request.executeSecondaryQueries();

      return beanCollection;

    } catch (SQLException e) {
      throw cquery.createPersistenceException(e);
      
    } finally {
      if (cquery != null) {
        cquery.close();
      }
      if (request.getQuery().isFutureFetch()) {
        // end the transaction for futureFindIds
        // as it had it's own transaction
        logger.debug("Future fetch completed!");
        request.getTransaction().end();
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

    try {
      cquery.prepareBindExecuteQuery();

      if (request.isLogSql()) {
        logSql(cquery);
      }

      if (cquery.readBean()) {
        bean = cquery.getLoadedBean();
      }

      if (request.isLogSummary()) {
        logFindBeanSummary(cquery);
      }

      request.executeSecondaryQueries();

      return (T)bean;

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

    String sql = query.getGeneratedSql();
    if (TransactionManager.SQL_LOGGER.isTraceEnabled()) {
      sql= Str.add(sql, "; --bind(", query.getBindLog(), ")");
    }
    query.getTransaction().logSql(sql);
  }

  /**
   * Log the FindById summary to the transaction log.
   */
  private void logFindBeanSummary(CQuery<?> q) {

    SpiQuery<?> query = q.getQueryRequest().getQuery();
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
    if (query.isAutofetchTuned()) {
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
    msg.append("exeMicros[").append(q.getQueryExecutionTimeMicros());
    msg.append("] rows[").append(q.getLoadedRowDetail());
    msg.append("] bind[").append(q.getBindLog()).append("]");

    q.getTransaction().logSummary(msg.toString());
  }

  /**
   * Log the FindMany to the transaction log.
   */
  private void logFindManySummary(CQuery<?> q) {

    SpiQuery<?> query = q.getQueryRequest().getQuery();
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
    if (query.isAutofetchTuned()) {
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
    msg.append("exeMicros[").append(q.getQueryExecutionTimeMicros());
    msg.append("] rows[").append(q.getLoadedRowDetail());
    msg.append("] name[").append(q.getName());
    msg.append("] predicates[").append(q.getLogWhereSql());
    msg.append("] bind[").append(q.getBindLog()).append("]");

    q.getTransaction().logSummary(msg.toString());
  }
}
