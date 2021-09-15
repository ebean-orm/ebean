package io.ebean.config;

import io.ebean.Database;
import io.ebean.meta.MetaQueryPlan;

import java.util.List;

/**
 * The captured query plans.
 */
public class QueryPlanCapture {

  private final Database database;
  private final List<MetaQueryPlan> plans;

  public QueryPlanCapture(Database database, List<MetaQueryPlan> plans) {
    this.database = database;
    this.plans = plans;
  }

  /**
   * Return the database the plans were captured for.
   */
  public Database database() {
    return database;
  }

  /**
   * Deprecated migrate to database().
   */
  @Deprecated
  public Database getDatabase() {
    return database;
  }

  /**
   * Return the captured query plans.
   */
  public List<MetaQueryPlan> plans() {
    return plans;
  }

  /**
   * Deprecated migrate to plans().
   */
  @Deprecated
  public List<MetaQueryPlan> getPlans() {
    return plans;
  }
}
