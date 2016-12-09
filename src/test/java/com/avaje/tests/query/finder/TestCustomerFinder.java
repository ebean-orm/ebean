package com.avaje.tests.query.finder;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCustomerFinder extends BaseTestCase {

  @Test
  public void test_ref() {

    ResetBasicData.reset();

    Customer customer = Customer.find.ref(1);
    assertThat(customer.getId()).isEqualTo(1);
  }

  @Test
  public void test_all_byId_byName() {

    ResetBasicData.reset();

    List<Customer> all = Customer.find.all();
    List<Customer> list = Ebean.find(Customer.class).findList();

    assertThat(all.size()).isEqualTo(list.size());

    Customer customer = all.get(0);

    Customer customer1 = Customer.find.byId(customer.getId());

    assertThat(customer.getId()).isEqualTo(customer1.getId());
    assertThat(customer.getName()).isEqualTo(customer1.getName());

    assertThat(Customer.find.db().getName()).isEqualTo(Ebean.getDefaultServer().getName());

  }

  @Test
  public void test_byName_deleteById() {

    Customer customer = new Customer();
    customer.setName("Newbie-879879897");

    Ebean.save(customer);
    assertThat(customer.getId()).isNotNull();

    Customer customer2 = Customer.find.byName(customer.getName());
    assertThat(customer.getId()).isEqualTo(customer2.getId());
    assertThat(customer.getName()).isEqualTo(customer2.getName());

    Customer.find.deleteById(customer.getId());
    awaitL2Cache();

    Customer notThere = Customer.find.byId(customer.getId());
    assertThat(notThere).isNull();
  }

  @Test
  public void test_nativeSingleAttribute() {

    ResetBasicData.reset();

    List<String> names = Customer.find.namesStartingWith("F");
    assertThat(names).isNotEmpty();
  }
}
