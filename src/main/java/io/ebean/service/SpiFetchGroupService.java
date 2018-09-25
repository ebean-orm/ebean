package io.ebean.service;

import io.ebean.FetchGroup;
import io.ebean.FetchGroupBuilder;

/**
 * Service that parses FetchGroup expressions.
 */
public interface SpiFetchGroupService {

  /**
   * Return the FetchGroup with the given select clause.
   *
   * @param beanType The type of entity bean the fetch group is for
   * @param select   The properties to select (top level properties)
   */
  <T> FetchGroup<T> of(Class<T> beanType, String select);

  /**
   * Create and return a FetchGroupBuilder starting with a select() clause.
   *
   * @param beanType The type of entity bean the fetch group is for
   * @return The FetchGroupBuilder to add additional select and fetch clauses
   */
  <T> FetchGroupBuilder<T> of(Class<T> beanType);
}
