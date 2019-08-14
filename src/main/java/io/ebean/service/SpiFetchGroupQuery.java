package io.ebean.service;

import io.ebean.FetchGroup;
import io.ebean.Query;

/**
 * Extension of Query to build FetchGroup via query beans.
 */
public interface SpiFetchGroupQuery<T> extends Query<T> {

  /**
   * Build the fetch group with select and fetch clauses.
   */
  FetchGroup<T> buildFetchGroup();
}
