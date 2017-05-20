package io.ebean.cache;

import io.ebean.config.CurrentTenantProvider;

import java.io.Serializable;
import java.util.Objects;

/**
 * Tenant aware handling for caching.
 */
public class TenantAwareKey {

  private final CurrentTenantProvider tenantProvider;

  /**
   * Construct with a tenant provider than can be null.
   */
  public TenantAwareKey(CurrentTenantProvider tenantProvider) {
    this.tenantProvider = tenantProvider;
  }

  /**
   * Return a tenant aware key.
   */
  public Object key(Object key) {
    if (tenantProvider != null) {
      return new CacheKey(key, tenantProvider.currentId());
    } else {
      return key;
    }
  }

  /**
   * We use a combined key, if this serverCache is per tenant.
   */
  public static final class CacheKey implements Serializable {

    private static final long serialVersionUID = 1L;

    final Object key;
    final Object tenantId;

    /**
     * Create with optional tenantId.
     */
    public CacheKey(Object key, Object tenantId) {
      this.key = key;
      this.tenantId = tenantId;
    }

    @Override
    public int hashCode() {
      int result = key.hashCode();
      result = 92821 * result + Objects.hashCode(tenantId);
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof CacheKey) {
        CacheKey that = (CacheKey) obj;
        return Objects.equals(that.key, this.key)
          && Objects.equals(that.tenantId, this.tenantId);
      }
      return false;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder(key.toString());
      if (tenantId != null) {
        sb.append(":").append(tenantId);
      }
      return sb.toString();
    }
  }
}
