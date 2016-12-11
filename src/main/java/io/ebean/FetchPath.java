package io.ebean;

import java.util.Set;

/**
 * Provides paths and properties for an object graph that can be used to control what parts of the object graph
 * is fetching (select and fetch clauses) and also can be used to control JSON marshalling (what parts of the object
 * graph are included in the JSON).
 */
public interface FetchPath {

  /**
   * Return true if the path is included in this FetchPath.
   */
  boolean hasPath(String path);

  /**
   * Return the properties at the given path.
   */
  Set<String> getProperties(String path);

  /**
   * Apply the fetch path to the query.
   */
  <T> void apply(Query<T> query);
}
