package io.ebean;

import javax.annotation.Nonnull;

/**
 * Builds a FetchGroup by adding fetch clauses.
 * <p>
 * We add select() and fetch() clauses to define the object graph we want to load.
 * </p>
 *
 * <pre>{@code
 *
 * FetchGroup fetchGroup = FetchGroup
 *   .select("name, status")
 *   .fetch("contacts", "firstName, lastName, email")
 *   .build();
 *
 * Customer.query()
 *   .select(fetchGroup)
 *   .where()
 *   ...
 *   .findList();
 *
 * }</pre>
 */
public interface FetchGroupBuilder<T> {

  /**
   * Specify specific properties to select (top level properties).
   */
  @Nonnull
  FetchGroupBuilder<T> select(String select);

  /**
   * Fetch all the properties at the given path.
   */
  @Nonnull
  FetchGroupBuilder<T> fetch(String path);

  /**
   * Fetch the path with the nested fetch group.
   */
  @Nonnull
  FetchGroupBuilder<T> fetch(String path, FetchGroup<?> nestedGroup);

  /**
   * Fetch the path using a query join with the nested fetch group.
   */
  @Nonnull
  FetchGroupBuilder<T> fetchQuery(String path, FetchGroup<?> nestedGroup);

  /**
   * Fetch the path lazily with the nested fetch group.
   */
  @Nonnull
  FetchGroupBuilder<T> fetchLazy(String path, FetchGroup<?> nestedGroup);

  /**
   * Fetch the path including specified properties.
   */
  @Nonnull
  FetchGroupBuilder<T> fetch(String path, String properties);

  /**
   * Fetch the path including all its properties using a query join.
   */
  @Nonnull
  FetchGroupBuilder<T> fetchQuery(String path);

  /**
   * Fetch the path including specified properties using a query join.
   */
  @Nonnull
  FetchGroupBuilder<T> fetchQuery(String path, String properties);

  /**
   * Fetch the path including all its properties lazily.
   */
  @Nonnull
  FetchGroupBuilder<T> fetchLazy(String path);

  /**
   * Fetch the path including specified properties lazily.
   */
  @Nonnull
  FetchGroupBuilder<T> fetchLazy(String path, String properties);

  /**
   * Build and return the FetchGroup.
   */
  @Nonnull
  FetchGroup<T> build();
}
