package io.ebeaninternal.server.querydefn;

import io.ebean.FetchGroup;

/**
 * Service API of FetchGroup.
 */
public interface SpiFetchGroup<T> extends FetchGroup<T> {

  /**
   * Return the detail to use for query execution.
   */
  OrmQueryDetail detail(OrmQueryDetail existing);

  /**
   * Return the underlying detail for copy purposes.
   */
  OrmQueryDetail underlying();
}
