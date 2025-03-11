package io.ebeaninternal.server.query;

import io.ebean.QueryIterator;
import io.ebean.Version;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.event.BeanFindController;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.OrmQueryEngine;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.core.SpiResultSet;
import io.ebeaninternal.server.persist.Binder;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public final class DefaultOrmQueryEngine implements OrmQueryEngine {

  private final CQueryEngine queryEngine;
  private final Binder binder;
  private final int forwardOnlyFetchSize;

  /**
   * Create the Finder.
   */
  public DefaultOrmQueryEngine(CQueryEngine queryEngine, Binder binder) {
    this.queryEngine = queryEngine;
    this.forwardOnlyFetchSize = queryEngine.forwardOnlyFetchSize();
    this.binder = binder;
  }

  @Override
  public <T> PersistenceException translate(OrmQueryRequest<T> request, String bindLog, String sql, SQLException e) {
    return queryEngine.translate(request, bindLog, sql, e);
  }

  @Override
  public int forwardOnlyFetchSize() {
    return forwardOnlyFetchSize;
  }

  @Override
  public boolean isMultiValueSupported(Class<?> cls) {
    return binder.isMultiValueSupported(cls);
  }

  /**
   * Flushes the jdbc batch by default unless explicitly turned off on the transaction.
   */
  private <T> void flushJdbcBatchOnQuery(OrmQueryRequest<T> request) {
    if (request.query().isUsingFuture()) {
      // future queries never invoke a flush
      return;
    }
    SpiTransaction t = request.transaction();
    if (t.isFlushOnQuery()) {
      // before we perform a query, we need to flush any
      // previous persist requests that are queued/batched.
      // The query may read data affected by those requests.
      t.flush();
    }
  }

  @Override
  public <T> int delete(OrmQueryRequest<T> request) {
    flushJdbcBatchOnQuery(request);
    return queryEngine.delete(request);
  }

  @Override
  public <T> int update(OrmQueryRequest<T> request) {
    flushJdbcBatchOnQuery(request);
    return queryEngine.update(request);
  }

  @Override
  public <T> SpiResultSet findResultSet(OrmQueryRequest<T> request) {
    flushJdbcBatchOnQuery(request);
    return queryEngine.findResultSet(request);
  }

  @Override
  public <T> int findCount(OrmQueryRequest<T> request) {
    flushJdbcBatchOnQuery(request);
    return queryEngine.findCount(request);
  }

  @Override
  public <A> List<A> findIds(OrmQueryRequest<?> request) {
    flushJdbcBatchOnQuery(request);
    return queryEngine.findIds(request);
  }

  @Override
  public <A extends Collection<?>> A findSingleAttributeCollection(OrmQueryRequest<?> request, A collection) {
    flushJdbcBatchOnQuery(request);
    return queryEngine.findSingleAttributeList(request, collection);
  }

  @Override
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

  @Override
  public <T> Object findMany(OrmQueryRequest<T> request) {
    flushJdbcBatchOnQuery(request);
    BeanFindController finder = request.finder();

    BeanCollection<T> result;
    if (finder != null && finder.isInterceptFindMany(request)) {
      // intercept this request
      result = finder.findMany(request);
    } else {
      result = queryEngine.findMany(request);
    }
    if (finder != null) {
      result = finder.postProcessMany(request, result);
    }

    if (result != null && request.isBeanCachePutMany()) {
      // load the individual beans into the bean cache
      request.descriptor().cacheBeanPutAll(result.actualDetails());
    }

    request.mergeCacheHits(result);
    Object finalResult = result;
    if (request.query().isUnmodifiable()) {
      finalResult = result == null ? null : result.freeze();
      if (request.isQueryCachePut()) {
        // load the query result into the query cache
        request.putToQueryCache(finalResult);
      }
    }
    return finalResult;
  }

  /**
   * Find a single bean using its unique id.
   */
  @Override
  public <T> T findId(OrmQueryRequest<T> request) {
    flushJdbcBatchOnQuery(request);
    BeanFindController finder = request.finder();

    T result;
    if (finder != null && finder.isInterceptFind(request)) {
      result = finder.find(request);
    } else {
      result = queryEngine.find(request);
    }
    if (finder != null) {
      result = finder.postProcess(request, result);
    }
    if (result != null && request.isBeanCachePut()) {
      request.descriptor().cacheBeanPut((EntityBean) result);
    }
    return result;
  }

}
