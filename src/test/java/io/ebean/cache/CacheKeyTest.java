package io.ebean.cache;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CacheKeyTest {

  @Test
  public void equals_when_null_tenantId() {

    TenantAwareKey.CacheKey key0 = key("12", null);
    TenantAwareKey.CacheKey key1 = key("12", null);

    assertMatchEquals(key0, key1);
  }

  private void assertMatchEquals(TenantAwareKey.CacheKey key0, TenantAwareKey.CacheKey key1) {
    assertThat(key0.hashCode()).isEqualTo(key1.hashCode());
    assertThat(key0.equals(key1)).isTrue();
    assertThat(key0.toString()).isEqualTo(key1.toString());
  }

  @Test
  public void equals_when_same_tenantId() {

    TenantAwareKey.CacheKey key0 = key(42L, "1");
    TenantAwareKey.CacheKey key1 = key(42L, "1");

    assertMatchEquals(key0, key1);
  }

  @Test
  public void not_equal_when_diff_both() {

    TenantAwareKey.CacheKey key0 = key(42L, "1");
    TenantAwareKey.CacheKey key1 = key(43L, "2");

    assertThat(key0.equals(key1)).isFalse();
  }

  @Test
  public void not_equal_when_diff_key() {

    TenantAwareKey.CacheKey key0 = key(42L, "1");
    TenantAwareKey.CacheKey key1 = key(43L, "1");

    assertThat(key0.equals(key1)).isFalse();
  }

  @Test
  public void not_equal_when_diff_key_andNoTenantId() {

    TenantAwareKey.CacheKey key0 = key(42L, null);
    TenantAwareKey.CacheKey key1 = key(43L, null);

    assertThat(key0.equals(key1)).isFalse();
  }

  @Test
  public void not_equal_when_diff_tenantId() {

    TenantAwareKey.CacheKey key0 = key(42L, "1");
    TenantAwareKey.CacheKey key1 = key(42L, "2");

    assertThat(key0.equals(key1)).isFalse();
  }

  private TenantAwareKey.CacheKey key(Object key, Object tenantId) {
    return new TenantAwareKey.CacheKey(key, tenantId);
  }
}
