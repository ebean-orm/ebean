package io.ebeaninternal.api;

/**
 * A SQL Join for a ToMany property included in the query.
 */
public interface SpiQueryManyJoin {

  /**
   * The full path of the many property to include in the query via join.
   */
  String path();

  /**
   * Order by clause defined via mapping on the ToMany property.
   */
  String fetchOrderBy();

  /**
   * Return true if this many relationship has an order column stored on the
   * ManyToMany intersection table (rather than a target descriptor property).
   */
  default boolean hasIntersectionOrderColumn() {
    return false;
  }

  /**
   * Return the db column name of the ManyToMany intersection table order column (or null).
   */
  default String intersectionOrderColumn() {
    return null;
  }

}
