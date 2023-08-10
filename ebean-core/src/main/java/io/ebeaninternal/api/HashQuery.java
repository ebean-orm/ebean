package io.ebeaninternal.api;

/**
 * A hash key for a query including both the query plan and bind values.
 */
public final class HashQuery {

  private final CQueryPlanKey planHash;
  private final BindValuesKey bindValuesKey;
  private final Class<?> dtoType;

  /**
   * Create the HashQuery.
   */
  public HashQuery(CQueryPlanKey planHash, BindValuesKey bindValuesKey, Class<?> dtoType) {
    this.planHash = planHash;
    this.bindValuesKey = bindValuesKey;
    this.dtoType = dtoType;
  }

  @Override
  public String toString() {
    return "HashQuery@" + Integer.toHexString(hashCode());
  }

  @Override
  public int hashCode() {
    int hc = 92821 * planHash.hashCode();
    hc = 92821 * hc + bindValuesKey.hashCode();
    hc = 92821 * hc + dtoType.hashCode();
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
    return e.bindValuesKey.equals(bindValuesKey) && e.planHash.equals(planHash) && e.dtoType.equals(dtoType);
  }
}
