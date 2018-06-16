package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.cache.ServerCache;
import org.junit.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryCacheTableDependency extends BaseTestCase {

  @Test
  public void testFindCountOnDependent() {

    ResetBasicData.reset();

    ServerCache customerCache = Ebean.getServerCacheManager().getQueryCache(Customer.class);
    customerCache.clear();

    List<Address> addrs = Ebean.find(Address.class)
      .where().eq("line2", "St Lukes")
      .findList();

    int custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lukes")
      .findCount();

    assertThat(custs).isEqualTo(3);

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lukes")
      .findCount();

    assertThat(custs).isEqualTo(3);

    Address a1 = addrs.get(0);
    a1.setLine2("St Lucky");
    Ebean.save(a1);

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lukes")
      .findCount();

    assertThat(custs).isEqualTo(2); // cache says 3


    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lucky")
      .findCount();

    assertThat(custs).isEqualTo(1);

  }
}
