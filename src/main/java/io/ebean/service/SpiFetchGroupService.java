package io.ebean.service;

import io.ebean.FetchGroup;
import io.ebean.FetchGroupBuilder;

/**
 * Service that parses FetchGroup expressions.
 */
public interface SpiFetchGroupService {

  /**
   * Return the FetchGroup with the given select clause.
   */
  FetchGroup of(String select);

  /**
   * Create and return a FetchGroupBuilder starting with a select() clause.
   *
   * @param select The properties to select (top level properties)
   * @return The FetchGroupBuilder to add additional fetch clauses
   */
  FetchGroupBuilder select(String select);
}
