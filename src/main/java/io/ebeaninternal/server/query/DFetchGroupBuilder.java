package io.ebeaninternal.server.query;

import io.ebean.FetchConfig;
import io.ebean.FetchGroup;
import io.ebean.FetchGroupBuilder;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.SpiFetchGroup;

/**
 * Default implementation of the FetchGroupBuilder.
 */
class DFetchGroupBuilder<T> implements FetchGroupBuilder<T> {

  private static final FetchConfig FETCH_QUERY = new FetchConfig().query();

  private static final FetchConfig FETCH_LAZY = new FetchConfig().lazy();

  private final OrmQueryDetail detail;

  DFetchGroupBuilder() {
    this.detail = new OrmQueryDetail();
  }

  @Override
  public FetchGroupBuilder<T> select(String select) {
    detail.select(select);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetch(String path) {
    detail.fetch(path, null, null);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetch(String path, FetchGroup nestedGroup) {
    return fetchNested(path, nestedGroup, null);
  }

  @Override
  public FetchGroupBuilder<T> fetchQuery(String path, FetchGroup nestedGroup) {
    return fetchNested(path, nestedGroup, FETCH_QUERY);
  }

  @Override
  public FetchGroupBuilder<T> fetchLazy(String path, FetchGroup nestedGroup) {
    return fetchNested(path, nestedGroup, FETCH_LAZY);
  }

  private FetchGroupBuilder<T> fetchNested(String path, FetchGroup nestedGroup, FetchConfig fetchConfig) {

    OrmQueryDetail nestedDetail = ((SpiFetchGroup) nestedGroup).underlying();
    detail.addNested(path, nestedDetail, fetchConfig);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetchQuery(String path) {
    detail.fetch(path, null, FETCH_QUERY);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetchLazy(String path) {
    detail.fetch(path, null, FETCH_LAZY);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetch(String path, String properties) {
    detail.fetch(path, properties, null);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetchQuery(String path, String properties) {
    detail.fetch(path, properties, FETCH_QUERY);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetchLazy(String path, String properties) {
    detail.fetch(path, properties, FETCH_LAZY);
    return this;
  }

  @Override
  public FetchGroup<T> build() {
    return new DFetchGroup<>(detail);
  }
}
