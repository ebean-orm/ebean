package io.ebeaninternal.server.querydefn;

import io.ebean.FetchGroup;

/**
 * Service API of FetchGroup.
 */
public interface SpiFetchGroup extends FetchGroup {

  /**
   * Return the detail to use for query execution.
   */
  OrmQueryDetail detail();

  /**
   * Return the underlying detail for copy purposes.
   */
  OrmQueryDetail underlying();
}
