package io.ebeaninternal.api;

import io.ebean.Transaction;
import io.ebeaninternal.server.core.OrmQueryRequest;

/**
 * Request for loading Associated One Beans.
 */
public abstract class LoadRequest {

  protected final OrmQueryRequest<?> parentRequest;

  protected final Transaction transaction;

  protected final boolean lazy;

  public LoadRequest(OrmQueryRequest<?> parentRequest, boolean lazy) {

    this.parentRequest = parentRequest;
    this.transaction = parentRequest == null ? null : parentRequest.getTransaction();
    this.lazy = lazy;
  }

  /**
   * Return the associated bean type for this load request.
   */
  public abstract Class<?> getBeanType();

  /**
   * Return true if this is a lazy load and false if it is a secondary query.
   */
  public boolean isLazy() {
    return lazy;
  }

  /**
   * Return the transaction to use if this is a secondary query.
   * <p>
   * Lazy loading queries run in their own transaction.
   * </p>
   */
  public Transaction getTransaction() {
    return transaction;
  }

  /**
   * Return true if the parent query is a findIterate() type query.
   * So one of - findIterate(), findEach(), findEachWhile() or findVisit().
   */
  public boolean isParentFindIterate() {
    return parentRequest != null && parentRequest.getQuery().getType() == SpiQuery.Type.ITERATE;
  }
}
