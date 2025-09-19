package io.ebeaninternal.server.query;

import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.SpiFetchGroup;

/**
 * Default FetchGroup implementation.
 */
final class DFetchGroup<T> implements SpiFetchGroup<T> {

  private final OrmQueryDetail detail;

  DFetchGroup(OrmQueryDetail detail) {
    this.detail = detail;
  }

  @Override
  public OrmQueryDetail detail(OrmQueryDetail existing) {
    return detail.copy(existing);
  }

  @Override
  public OrmQueryDetail underlying() {
    return detail;
  }
}
