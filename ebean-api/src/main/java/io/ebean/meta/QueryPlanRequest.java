package io.ebean.meta;

/**
 * Request used to capture query plans.
 */
public final class QueryPlanRequest {

  private long since;

  private final int maxCount;

  private final long maxTimeMillis;

  /**
   * Create with the max number of plans and max capture time.
   *
   * @param maxCount      The maximum number of plans to capture
   * @param maxTimeMillis The maximum time after which we stop capturing more plans
   */
  public QueryPlanRequest(int maxCount, long maxTimeMillis) {
    this.maxCount = maxCount;
    this.maxTimeMillis = maxTimeMillis;
  }

  /**
   * Return the epoch time in millis for minimum bind capture time.
   * <p>
   * When set this ensures that the bind values used to get the query plan
   * have been around for a while (e.g. 5 mins) and so reasonably represent
   * bind values that match the slowest execution for this query plan.
   */
  public long since() {
    return since;
  }

  /**
   * Set the epoch time (e.g. 5 mins ago) such that the query bind values
   * reasonably represent bind values that match the slowest execution for this query plan.
   *
   * @param since The minimum age of the bind values capture.
   */
  public void since(long since) {
    this.since = since;
  }

  /**
   * Return the maximum number of plans to capture.
   */
  public int maxCount() {
    return maxCount;
  }

  /**
   * Return the maximum amount of time we want to use to capture plans.
   * <p>
   * Query plan collection will stop once this time is exceeded.
   */
  public long maxTimeMillis() {
    return maxTimeMillis;
  }
}
