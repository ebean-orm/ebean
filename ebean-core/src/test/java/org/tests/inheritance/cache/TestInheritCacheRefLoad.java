package org.tests.inheritance.cache;

import io.ebean.DB;
import org.junit.Assert;
import org.junit.Test;

public class TestInheritCacheRefLoad {

  @Test
  public void findWithRefToInheritStreetBean_expect_correctStreetType() {

    DB.find(CICustomerParent.class).delete();
    DB.find(CIAddress.class).delete();
    DB.find(CIStreet.class).delete();

    //=========================================================================
    // 1 - Create Data (Customer(dtype 1) -> Address(no dtype) -> Street(dtype 1)
    //=========================================================================
    CIStreet street = new CIStreet();
    street.save();
    CIAddress address = new CIAddress();
    address.setStreet(street);
    address.save();
    CICustomer customer = new CICustomer();
    customer.setAddress(address);
    customer.save();

    //=========================================================================
    // 2 - Read Data (no Cache Hit)
    //=========================================================================
    Class streetClass = reloadCustomer(customer, false);  //returns Street -> OK
    Assert.assertEquals("org.tests.inheritance.cache.CIStreet", streetClass.getName());
    streetClass = reloadCustomer(customer, false);  //returns Street -> OK
    Assert.assertEquals("org.tests.inheritance.cache.CIStreet", streetClass.getName());

    //=========================================================================
    // 3 - Read Data (L2-Cache Hit)
    //=========================================================================
    streetClass = reloadCustomer(customer, true); //returns Street -> OK
    Assert.assertEquals("org.tests.inheritance.cache.CIStreet", streetClass.getName());
    streetClass = reloadCustomer(customer, true); //returns StreetParent -> NOT OK
    Assert.assertEquals("org.tests.inheritance.cache.CIStreet", streetClass.getName());
  }


  public Class reloadCustomer(CICustomer customer, boolean l2Cache) {

    // Load Customer via Query, L2Cache on/off
    CICustomer customerReloaded =
      DB.find(CICustomer.class)
        .where().eq("id", customer.getId())
        .setUseCache(l2Cache)
        .findOne();

    //Access Street by lazy Loading
    Class streetClassLazyLoaded = customerReloaded.getAddress().getStreet().getClass();

    //Show Cache Hits
    System.out.println("Class of Street (Cache on: " + l2Cache + "): " + streetClassLazyLoaded + " | Cache Hit: " + DB.getDefault().getServerCacheManager().getQueryCache(streetClassLazyLoaded).getStatistics(false).getHitCount());
    return streetClassLazyLoaded;
  }

}
