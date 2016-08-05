package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.Version;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.event.BeanFindController;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.OrmQueryEngine;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.Collection;
import java.util.List;

/**
 * Main Finder implementation.
 */
public class DefaultOrmQueryEngine implements OrmQueryEngine {

  /**
   * Find using predicates
   */
  private final CQueryEngine queryEngine;

  /**
   * Create the Finder.
   */
  public DefaultOrmQueryEngine(CQueryEngine queryEngine) {

    this.queryEngine = queryEngine;
  }

  /**
   * Flushes the jdbc batch by default unless explicitly turned off on the transaction.
   */
  private <T> void flushJdbcBatchOnQuery(OrmQueryRequest<T> request) {

    SpiTransaction t = request.getTransaction();
    if (t.isBatchFlushOnQuery()) {
      // before we perform a query, we need to flush any
      // previous persist requests that are queued/batched.
      // The query may read data affected by those requests.
      t.flushBatch();
    }
  }

  public <T> int delete(OrmQueryRequest<T> request) {

    flushJdbcBatchOnQuery(request);
    return queryEngine.delete(request);
  }

  public <T> int update(OrmQueryRequest<T> request) {

    flushJdbcBatchOnQuery(request);
    return queryEngine.update(request);
  }

  public <T> int findRowCount(OrmQueryRequest<T> request) {

    flushJdbcBatchOnQuery(request);
    return queryEngine.findRowCount(request);
  }

  public <A> List<A> findIds(OrmQueryRequest<?> request) {

    flushJdbcBatchOnQuery(request);
    return queryEngine.findIds(request);
  }

  @Override
  public <A> List<A> findSingleAttributeList(OrmQueryRequest<?> request) {
    flushJdbcBatchOnQuery(request);
    return queryEngine.findSingleAttributeList(request);
  }

  public <T> QueryIterator<T> findIterate(OrmQueryRequest<T> request) {

    // LIMITATION: You can not use QueryIterator to load bean cache

    flushJdbcBatchOnQuery(request);
    return queryEngine.findIterate(request);
  }

  @Override
  public <T> List<Version<T>> findVersions(OrmQueryRequest<T> request) {

    flushJdbcBatchOnQuery(request);
    return queryEngine.findVersions(request);
  }

  public <T> BeanCollection<T> findMany(OrmQueryRequest<T> request) {

    flushJdbcBatchOnQuery(request);

    BeanFindController finder = request.getBeanFinder();

    BeanCollection<T> result;
    if (finder != null && finder.isInterceptFindMany(request)) {
      // intercept this request
      result = finder.findMany(request);
    } else {
      result = queryEngine.findMany(request);
    }

    SpiQuery<T> query = request.getQuery();

    if (query.isLoadBeanCache()) {
      // load the individual beans into the bean cache
      BeanDescriptor<T> descriptor = request.getBeanDescriptor();
      Collection<T> c = result.getActualDetails();
      for (T bean : c) {
        descriptor.cacheBeanPut((EntityBean) bean);
      }
    }

    if (!result.isEmpty() && query.isUseQueryCache()) {
      // load the query result into the query cache
      request.putToQueryCache(result);
    }

    return result;
  }

  /**
   * Find a single bean using its unique id.
   */
  public <T> T findId(OrmQueryRequest<T> request) {

    flushJdbcBatchOnQuery(request);

    BeanFindController finder = request.getBeanFinder();

    T result;
    if (finder != null && finder.isInterceptFind(request)) {
      result = finder.find(request);
    } else {
      result = queryEngine.find(request);
    }

    if (result != null && request.isUseBeanCache()) {
      request.getBeanDescriptor().cacheBeanPut((EntityBean) result);
    }

    return result;
  }

}
