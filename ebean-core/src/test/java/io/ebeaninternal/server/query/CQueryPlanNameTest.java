package io.ebeaninternal.server.query;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the query plan / metric name derivation.
 */
class CQueryPlanNameTest {

  @Test
  void deriveName_root_unlabelled() {
    assertThat(CQueryPlan.deriveName(null, false, false, "findList", "Customer"))
      .isEqualTo("orm.Customer.findList");
  }

  @Test
  void deriveName_root_explicitLabel() {
    assertThat(CQueryPlan.deriveName("custMain", false, true, "findList", "Customer"))
      .isEqualTo("orm.Customer.custMain");
  }

  @Test
  void deriveName_root_profileLocation_startsWithBeanType() {
    // profile location used as-is (no bean type prefix)
    assertThat(CQueryPlan.deriveName("CustomerFinder.byName", false, false, "findList", "Customer"))
      .isEqualTo("orm.CustomerFinder.byName");
  }

  @Test
  void deriveName_root_profileLocation_differentClass() {
    // profile location is a unique, type-independent identifier - not prefixed with the bean type
    assertThat(CQueryPlan.deriveName("DataLoader.loadAll", false, false, "findList", "Customer"))
      .isEqualTo("orm.DataLoader.loadAll");
  }

  @Test
  void deriveName_secondary_usesLabelVerbatim() {
    // secondary query label already carries the full parent name + path + load mode
    assertThat(CQueryPlan.deriveName("Customer.custMain.contacts.lazy", true, true, "findList", "Contact"))
      .isEqualTo("orm.Customer.custMain.contacts.lazy");
    assertThat(CQueryPlan.deriveName("CustomerFinder.byName.contacts.lazy", true, true, "findList", "Contact"))
      .isEqualTo("orm.CustomerFinder.byName.contacts.lazy");
  }

  @Test
  void planLabelWithType_explicitLabel() {
    assertThat(CQueryPlan.planLabelWithType("custMain", "Customer"))
      .isEqualTo("Customer.custMain");
  }

  @Test
  void planLabelWithType_startsWithBeanType() {
    assertThat(CQueryPlan.planLabelWithType("CustomerFinder.byName", "Customer"))
      .isEqualTo("CustomerFinder.byName");
  }

  @Test
  void planLabelWithType_differentClass() {
    assertThat(CQueryPlan.planLabelWithType("DataLoader.loadAll", "Customer"))
      .isEqualTo("Customer.DataLoader.loadAll");
  }

  @Test
  void planLabelWithType_elementCollection() {
    // element collection descriptor simple name contains a dot - keeps underscore form
    assertThat(CQueryPlan.planLabelWithType("custMain", "Customer.phones"))
      .isEqualTo("Customer_custMain");
  }
}
