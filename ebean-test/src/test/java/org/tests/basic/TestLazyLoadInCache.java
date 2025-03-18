package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.CacheMode;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestLazyLoadInCache extends BaseTestCase {

  @Test
  public void testLoadInCache() {

    ResetBasicData.reset();

    Map<Integer, Customer> map = DB.find(Customer.class)
      .select("id, name, status, billingAddress")
      .setBeanCacheMode(CacheMode.PUT)
      .setUnmodifiable(true)
      .orderBy().asc("id")
      .findMap();

    assertFalse(map.isEmpty());

    Object id = map.keySet().iterator().next();

    Customer cust1 = map.get(id);

    Customer cust1B = DB.find(Customer.class)
      .setUnmodifiable(true)
      .setUseCache(false)
      .setId(id)
      .findOne();

    assertNotSame(cust1, cust1B);

    Set<String> loadedProps = DB.beanState(cust1).loadedProps();

    assertThat(loadedProps).contains("id", "name", "status");
    // cust1.getStatus(); // can't lazy load with unmodifiable
    assertThat(DB.beanState(cust1).isUnmodifiable()).isTrue();

    // a readOnly reference
    Address billingAddress = cust1.getBillingAddress();
    BeanState billAddrState = DB.beanState(billingAddress);
    // assertTrue(billAddrState.isReference()); // not supported by unmodifiable
    assertTrue(billAddrState.isUnmodifiable());

    // lazy load .. no longer a reference
    // billingAddress.getCity(); // lazy loading not supported by unmodifiable
    // assertFalse(billAddrState.isReference());

  }

}
