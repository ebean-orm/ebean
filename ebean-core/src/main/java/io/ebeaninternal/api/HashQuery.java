package io.ebeaninternal.api;

/**
 * A hash key for a query including both the query plan and bind values.
 */
public class HashQuery {

  private final CQueryPlanKey planHash;

  private final BindHash bindHash;

  /**
   * Create the HashQuery.
   */
  public HashQuery(CQueryPlanKey planHash, BindHash bindHash) {
    this.planHash = planHash;
    this.bindHash = bindHash;
  }

  @Override
  public String toString() {
    return "HashQuery@" + Integer.toHexString(hashCode());
  }

  @Override
  public int hashCode() {
    int hc = 92821 * planHash.hashCode();
    hc = 92821 * hc + bindHash.hashCode();
    return hc;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof HashQuery)) {
      return false;
    }

    HashQuery e = (HashQuery) obj;
    return e.bindHash.equals(bindHash) && e.planHash.equals(planHash);
  }
}
