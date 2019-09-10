package io.ebeaninternal.server.query;

import io.ebean.FetchGroup;
import io.ebean.FetchGroupBuilder;
import io.ebean.service.SpiFetchGroupQuery;
import io.ebean.service.SpiFetchGroupService;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;

/**
 * Default implementation of SpiFetchGroupService.
 */
public final class DFetchGroupService implements SpiFetchGroupService {

  @Override
  public <T> FetchGroup<T> of(Class<T> cls, String select) {
    return new DFetchGroup<>(detail(select));
  }

  @Override
  public <T> FetchGroupBuilder<T> of(Class<T> cls) {
    return new DFetchGroupBuilder<>();
  }

  @Override
  public <T> SpiFetchGroupQuery<T> queryFor(Class<T> beanType) {
    return new DefaultFetchGroupQuery<>();
  }

  private OrmQueryDetail detail(String select) {
    OrmQueryDetail detail = new OrmQueryDetail();
    detail.select(select);
    return detail;
  }
}
