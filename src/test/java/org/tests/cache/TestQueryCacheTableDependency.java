package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.cache.ServerCache;
import org.junit.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Contact;
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

    assertThat(custs).isEqualTo(2);


    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lucky")
      .findCount();

    assertThat(custs).isEqualTo(1);

    Ebean.update(Address.class)
      .set("line2", "St Lucky2")
      .where().eq("line2", "St Lucky")
      .update();

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lucky")
      .findCount();

    assertThat(custs).isEqualTo(0);

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lucky2")
      .findCount();
    assertThat(custs).isEqualTo(1);

    Ebean.createSqlUpdate("update o_address set line_2=? where line_2=?")
      .setNextParameter("St Lucky3")
      .setNextParameter("St Lucky2")
      .execute();

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lucky2")
      .findCount();
    assertThat(custs).isEqualTo(0);

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lucky3")
      .findCount();

    assertThat(custs).isEqualTo(1);

  }

  @Test
  public void testFindCountOnOtherL2Cached() {

    ResetBasicData.reset();

    Customer fi = Ebean.find(Customer.class).where().eq("name", "Fiona").findOne();

    int custCount0 = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where()
      .eq("name", "Fiona")
      .isNull("contacts.phone")
      .findCount();

    assertThat(custCount0).isEqualTo(1);

    int updateRows = Ebean.update(Contact.class)
      .set("phone", "1234")
      .where()
      .eq("customer.id", fi.getId())
      .update();

    assertThat(updateRows).isGreaterThan(0);

    int custCount1 = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where()
      .eq("name", "Fiona")
      .isNull("contacts.phone")
      .findCount();

    assertThat(custCount1).isEqualTo(0);

  }
}
