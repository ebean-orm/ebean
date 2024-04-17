package io.ebean.config.dbplatform;

/**
 * Insert SQL syntax to allow support for ClickHouse type optimisation for inserts.
 */
public interface InsertSqlSyntaxExtension {

  /**
   * Start the columns.
   */
  String startColumns();

  /**
   * End of the columns.
   */
  String endColumns();

  /**
   * Return true for insert to use standard binding.
   */
  boolean useBinding();

  /**
   * Start types for non-standard binding (e.g. ClickHouse).
   */
  String startTypes();

  /**
   * End types for non-standard binding (e.g. ClickHouse).
   */
  String endTypes();
}
