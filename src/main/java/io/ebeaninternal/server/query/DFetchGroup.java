package io.ebeaninternal.server.query;

import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.SpiFetchGroup;

/**
 * Default FetchGroup implementation.
 */
class DFetchGroup<T> implements SpiFetchGroup<T> {

  private final OrmQueryDetail detail;

  DFetchGroup(OrmQueryDetail detail) {
    this.detail = detail;
  }

  @Override
  public OrmQueryDetail detail() {
    return detail.copy();
  }

  @Override
  public OrmQueryDetail underlying() {
    return detail;
  }
}
