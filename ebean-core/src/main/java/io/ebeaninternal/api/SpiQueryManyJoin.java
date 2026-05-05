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

}
