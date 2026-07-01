package io.ebeaninternal.server.query;

import org.jspecify.annotations.NullMarked;
import io.ebean.FetchConfig;
import io.ebean.FetchGroup;
import io.ebean.FetchGroupBuilder;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.SpiFetchGroup;

/**
 * Default implementation of the FetchGroupBuilder.
 */
@NullMarked
final class DFetchGroupBuilder<T> implements FetchGroupBuilder<T> {

  private static final FetchConfig DEFAULT_FETCH = FetchConfig.ofDefault();
  private static final FetchConfig FETCH_CACHE = FetchConfig.ofCache();
  private static final FetchConfig FETCH_QUERY = FetchConfig.ofQuery();
  private static final FetchConfig FETCH_LAZY = FetchConfig.ofLazy();

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
    detail.fetchProperties(path, null, DEFAULT_FETCH);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetch(String path, FetchGroup<?> nestedGroup) {
    return fetchNested(path, nestedGroup, DEFAULT_FETCH);
  }

  @Override
  public FetchGroupBuilder<T> fetchQuery(String path, FetchGroup<?> nestedGroup) {
    return fetchNested(path, nestedGroup, FETCH_QUERY);
  }

  @Override
  public FetchGroupBuilder<T> fetchLazy(String path, FetchGroup<?> nestedGroup) {
    return fetchNested(path, nestedGroup, FETCH_LAZY);
  }

  private FetchGroupBuilder<T> fetchNested(String path, FetchGroup<?> nestedGroup, FetchConfig fetchConfig) {
    OrmQueryDetail nestedDetail = ((SpiFetchGroup<?>) nestedGroup).underlying();
    detail.addNested(path, nestedDetail, fetchConfig);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetchQuery(String path) {
    detail.fetchProperties(path, null, FETCH_QUERY);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetchCache(String path) {
    detail.fetchProperties(path, null, FETCH_CACHE);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetchLazy(String path) {
    detail.fetchProperties(path, null, FETCH_LAZY);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetch(String path, String properties) {
    detail.fetch(path, properties, DEFAULT_FETCH);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetchQuery(String path, String properties) {
    detail.fetch(path, properties, FETCH_QUERY);
    return this;
  }

  @Override
  public FetchGroupBuilder<T> fetchCache(String path, String properties) {
    detail.fetch(path, properties, FETCH_CACHE);
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
