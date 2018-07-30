package io.ebeaninternal.server.query;

import io.ebean.FetchConfig;
import io.ebean.FetchGroup;
import io.ebean.FetchGroupBuilder;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.SpiFetchGroup;

/**
 * Default implementation of the FetchGroupBuilder.
 */
class DFetchGroupBuilder implements FetchGroupBuilder {

  private static final FetchConfig FETCH_QUERY = new FetchConfig().query();

  private static final FetchConfig FETCH_LAZY = new FetchConfig().lazy();

  private OrmQueryDetail detail;

  DFetchGroupBuilder(OrmQueryDetail detail) {
    this.detail = detail;
  }

  @Override
  public FetchGroupBuilder fetch(String path) {
    detail.fetch(path, null, null);
    return this;
  }

  @Override
  public FetchGroupBuilder fetch(String path, FetchGroup nestedGroup) {
    return fetchNested(path, nestedGroup, null);
  }
  @Override
  public FetchGroupBuilder fetchQuery(String path, FetchGroup nestedGroup) {
    return fetchNested(path, nestedGroup, FETCH_QUERY);
  }

  @Override
  public FetchGroupBuilder fetchLazy(String path, FetchGroup nestedGroup) {
    return fetchNested(path, nestedGroup, FETCH_LAZY);
  }

  private FetchGroupBuilder fetchNested(String path, FetchGroup nestedGroup, FetchConfig fetchConfig) {

    OrmQueryDetail nestedDetail = ((SpiFetchGroup) nestedGroup).underlying();
    detail.addNested(path, nestedDetail, fetchConfig);
    return this;
  }

  @Override
  public FetchGroupBuilder fetchQuery(String path) {
    detail.fetch(path, null, FETCH_QUERY);
    return this;
  }

  @Override
  public FetchGroupBuilder fetchLazy(String path) {
    detail.fetch(path, null, FETCH_LAZY);
    return this;
  }

  @Override
  public FetchGroupBuilder fetch(String path, String properties) {
    detail.fetch(path, properties, null);
    return this;
  }

  @Override
  public FetchGroupBuilder fetchQuery(String path, String properties) {
    detail.fetch(path, properties, FETCH_QUERY);
    return this;
  }

  @Override
  public FetchGroupBuilder fetchLazy(String path, String properties) {
    detail.fetch(path, properties, FETCH_LAZY);
    return this;
  }

  @Override
  public FetchGroup build() {
    return new DFetchGroup(detail);
  }
}
