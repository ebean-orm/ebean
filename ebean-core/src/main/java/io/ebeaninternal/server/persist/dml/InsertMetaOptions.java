package io.ebeaninternal.server.persist.dml;

import io.ebean.InsertOptions;

/**
 * Generator for insert SQL with options.
 */
interface InsertMetaOptions {

  /**
   * Generate the SQL for the given insert options.
   */
  String sql(boolean withId, InsertOptions insertOptions);
}
