package com.avaje.ebeaninternal.server.query;

import java.util.Collection;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.event.BeanFinder;
import com.avaje.ebeaninternal.api.BeanIdList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.OrmQueryEngine;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;

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
  public DefaultOrmQueryEngine(BeanDescriptorManager descMgr, CQueryEngine queryEngine) {

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

  public <T> int findRowCount(OrmQueryRequest<T> request) {

    flushJdbcBatchOnQuery(request);
    return queryEngine.findRowCount(request);
  }

  public <T> BeanIdList findIds(OrmQueryRequest<T> request) {

    flushJdbcBatchOnQuery(request);
    return queryEngine.findIds(request);
  }

  public <T> QueryIterator<T> findIterate(OrmQueryRequest<T> request) {

    // LIMITATION: You can not use QueryIterator to load bean cache

    flushJdbcBatchOnQuery(request);
    return queryEngine.findIterate(request);
  }

  public <T> BeanCollection<T> findMany(OrmQueryRequest<T> request) {

    flushJdbcBatchOnQuery(request);

    BeanFinder<T> finder = request.getBeanFinder();

    BeanCollection<T> result;
    if (finder != null) {
      // this bean type has its own specific finder
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
        descriptor.cacheBeanPutData((EntityBean) bean);
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

    BeanFinder<T> finder = request.getBeanFinder();

    T result;
    if (finder != null) {
      result = finder.find(request);
    } else {
      result = queryEngine.find(request);
    }

    if (result != null && request.isUseBeanCache()) {
      request.getBeanDescriptor().cacheBeanPutData((EntityBean) result);
    }

    return result;
  }

}
