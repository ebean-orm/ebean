package io.ebean.meta;

/**
 * Request used to capture query plans.
 */
public class QueryPlanRequest {

  private long since;

  private int maxCount;

  private long maxTimeMillis;

  /**
   * Return the epoch time in millis for minimum bind capture time.
   * <p>
   * When set this ensures that the bind values used to get the query plan
   * have been around for a while (e.g. 5 mins) and so reasonably represent
   * bind values that match the slowest execution for this query plan.
   */
  public long getSince() {
    return since;
  }

  /**
   * Set the epoch time (e.g. 5 mins ago) such that the query bind values
   * reasonably represent bind values that match the slowest execution for this query plan.
   *
   * @param since The minimum age of the bind values capture.
   */
  public void setSince(long since) {
    this.since = since;
  }

  /**
   * Return the maximum number of plans to capture.
   */
  public int getMaxCount() {
    return maxCount;
  }

  /**
   * Set the maximum number of plans to capture.
   */
  public void setMaxCount(int maxCount) {
    this.maxCount = maxCount;
  }

  /**
   * Return the maximum amount of time we want to use to capture plans.
   * <p>
   * Query plan collection will stop once this time is exceeded.
   */
  public long getMaxTimeMillis() {
    return maxTimeMillis;
  }

  /**
   * Set the maximum amount of time we want to use to capture plans.
   * <p>
   * Query plan collection will stop once this time is exceeded.
   */
  public void setMaxTimeMillis(long maxTimeMillis) {
    this.maxTimeMillis = maxTimeMillis;
  }
}
