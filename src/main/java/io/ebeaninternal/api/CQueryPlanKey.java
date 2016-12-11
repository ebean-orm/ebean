package io.ebeaninternal.api;

/**
 * Key used for caching query plans for ORM and RawSql queries.
 */
public interface CQueryPlanKey {

  /**
   * Used by read audit such that we can log read audit entries without the full sql
   * (which would make the read audit logs verbose).
   */
  String getPartialKey();

}
