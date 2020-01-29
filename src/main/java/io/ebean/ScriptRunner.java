package io.ebean;

import java.net.URL;
import java.util.Map;

/**
 * Runs DDL and SQL scripts.
 * <p/>
 * Typically these are scripts used for testing such as seed SQL scripts or truncate SQL scripts.
 * <p/>
 * Scripts are executed in their own transaction and committed on successful completion.
 *
 * <h3>Example of simple use</h3>
 * <pre>{@code
 *
 *   Database database = DB.getDefault();
 *   database.script().run("/scripts/test-script.sql");
 *
 * }</pre>
 */
public interface ScriptRunner {

  /**
   * Run a script given the resource path (that should start with "/").
   */
  void run(String path);

  /**
   * Run a script given the resource path (that should start with "/") and place holders.
   *
   * <pre>{@code
   *
   *   Map<String,String> placeholders = new HashMap<>();
   *   placeholders.put("tableName", "e_basic");
   *
   *   Database database = DB.getDefault();
   *   database.script().run("/scripts/test-script.sql", placeholders);
   *
   * }</pre>
   */
  void run(String path, Map<String, String> placeholderMap);

  /**
   * Run a DDL or SQL script given the resource.
   */
  void run(URL resource);

  /**
   * Run a DDL or SQL script given the resource and place holders.
   */
  void run(URL resource, Map<String, String> placeholderMap);

  /**
   * Run the raw provided DDL or SQL script.
   *
   * @param name          The name of the script for logging purposes
   * @param content       The SQL content
   * @param useAutoCommit Set to true to use auto commit true and continue when any errors occur
   */
  void runScript(String name, String content, boolean useAutoCommit);

}
