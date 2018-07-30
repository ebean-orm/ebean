package io.ebeaninternal.server.query;

import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.SpiFetchGroup;

/**
 * Default FetchGroup implementation.
 */
class DFetchGroup implements SpiFetchGroup {

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
