package io.ebean.meta;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Request used to capture query plans.
 */
public class QueryPlanRequest {

  private final List<MetaQueryPlan> plans = new ArrayList<>();

  private Connection connection;

  private boolean store;

  private long since;

  private Set<Class<?>> includedBeanTypes;

  private Set<String> includedLabels;

  public List<MetaQueryPlan> getPlans() {
    return plans;
  }

  /**
   * Return the connection to use to capture the query plans.
   */
  public Connection getConnection() {
    return connection;
  }

  /**
   * Set the connection to use to capture the query plans.
   */
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  /**
   * Return true if the captured query plan is stored.
   */
  public boolean isStore() {
    return store;
  }

  /**
   * Set to true to store the captured query plan.
   */
  public void setStore(boolean store) {
    this.store = store;
  }

  /**
   * Return the epoch time after which the query plan was capture (to be included).
   */
  public long getSince() {
    return since;
  }

  /**
   * Set the epoch time after which the query plan was captured.
   * <p>
   * This is used to only capture plans that have changed since a given time (like the time of last capture).
   * </p>
   *
   * @param since The time after which the query plan was captured to be included
   */
  public void setSince(long since) {
    this.since = since;
  }

  /**
   * Process consume the query plan.
   */
  public void process(MetaQueryPlan plan) {
    plans.add(plan);
  }

  /**
   * Return true if the bean type should be included in the query plan capture.
   */
  public boolean includeType(Class<?> beanType) {
    return includedBeanTypes == null || includedBeanTypes.isEmpty() || includedBeanTypes.contains(beanType);
  }

  /**
   * Return true if the label should be included in the query plan capture.
   */
  public boolean includeLabel(String label) {
    return includedLabels == null || includedLabels.isEmpty() || includedLabels.contains(label);
  }

}
