package org.tests.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Filter;
import io.ebean.Query;
import io.ebean.QueryDsl;

/**
 * Test the in memory filtering.
 * @author Roland Praml, FOCONIS AG
 *
 */
public class TestInMemoryQuery extends BaseTestCase {

  private Customer rob;
  private Customer custNoAddress;
  private Customer fiona;
  private Customer nocCust;

  @Before
  public void setup() {
    ResetBasicData.reset();
    rob = Ebean.find(Customer.class, 1);
    custNoAddress = Ebean.find(Customer.class, 2);
    fiona = Ebean.find(Customer.class, 3);
    nocCust = Ebean.find(Customer.class, 4);
  }

  @Test
  public void testNull() {

    testQuery(condition -> {
      condition.eq("orders.details.product.name", "Chair");
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.eq("orders.details.product.name", "Desk");
    }, rob);

    testQuery(condition -> {
      condition.isNull("orders.details.product.name");
    }, rob, custNoAddress, fiona, nocCust);

    testQuery(condition -> {
      condition.isNull("orders");
    }, fiona, nocCust);

    testQuery(condition -> {
      condition.isNotNull("orders");
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.isNotNull("orders.details.product.name");
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.or() //
      .isNull("orders.details.product.name")
      .isNull("orders") //
      .endOr();
    }, rob, custNoAddress, fiona, nocCust);
  }

  @Test
  public void testEq() {
    Order order1 = Ebean.find(Order.class).findList().get(0);

    testQuery(condition -> {
      condition.eq("orders.details.product.name", "Chair");
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.eq("orders", order1);
    }, rob);

    testQuery(condition -> {
      condition.ne("orders", order1);
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.eq("orders.id", 1);
    }, rob);

    testQuery(condition -> {
      condition.ne("orders.id", 1);
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.eq("orders.status", Order.Status.SHIPPED);
    }, custNoAddress);
    testQuery(condition -> {
      condition.ne("orders.status", Order.Status.SHIPPED);
    }, rob, custNoAddress);
  }

  @Test
  public void testString() {

    testQuery(condition -> {
      condition.eq("orders.details.product.name", "chair");
    });

    testQuery(condition -> {
      condition.ieq("orders.details.product.name", "chair");
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.contains("orders.details.product.name", "e");
    }, rob);

    testQuery(condition -> {
      condition.contains("orders.details.product.name", "E");
    });

    testQuery(condition -> {
      condition.icontains("orders.details.product.name", "E");
    }, rob);

    testQuery(condition -> {
      condition.startsWith("name", "R");
    }, rob);

    testQuery(condition -> {
      condition.startsWith("orders.details.product.name", "C");
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.startsWith("orders.details.product.name", "c");
    });

    testQuery(condition -> {
      condition.istartsWith("orders.details.product.name", "c");
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.endsWith("orders.details.product.name", "r");
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.endsWith("orders.details.product.name", "R");
    });

    testQuery(condition -> {
      condition.iendsWith("orders.details.product.name", "R");
    }, rob, custNoAddress);
  }

  @Test
  public void testComp() {

    testQuery(condition -> {
      condition.gt("name", "Fiona");
    }, rob, nocCust);

    testQuery(condition -> {
      condition.ge("name", "Fiona");
    }, rob, fiona, nocCust);

    testQuery(condition -> {
      condition.lt("name", "Fiona");
    }, custNoAddress);

    testQuery(condition -> {
      condition.le("name", "Fiona");
    }, custNoAddress, fiona);
  }

  @Test
  public void testBetween() {

    testQuery(condition -> {
      condition.between("id", 2.0, 3.0);
    }, custNoAddress, fiona);

    testQuery(condition -> {
      condition.between("id", 2.0, 4.0);
    }, custNoAddress, fiona, nocCust);

    testQuery(condition -> {
      condition.between("id", 2, 3);
    }, custNoAddress, fiona);

    testQuery(condition -> {
      condition.between("id", 3, 2);
    });
  }

  @Test
  public void testIn() throws Exception {

    testQuery(condition -> {
      condition.in("status", Customer.Status.NEW);
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.in("status", Customer.Status.NEW, Customer.Status.ACTIVE);
    }, rob, custNoAddress, fiona, nocCust);

    testQuery(condition -> {
      condition.notIn("status", Customer.Status.NEW);
    }, fiona, nocCust);

    testQuery(condition -> {
      condition.notIn("status", Customer.Status.NEW, Customer.Status.INACTIVE);
    }, fiona, nocCust);

  }

  @Test
  public void testInSubQuery() throws Exception {

    Query<Order> shippedOrders = Ebean.find(Order.class)
        .select("id") // = 1,4,5
        .where().eq("status", Order.Status.NEW).query();

    testQuery(condition -> {
      condition.in("orders.id", shippedOrders.findSingleAttributeList());
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.notIn("orders.id", shippedOrders.findSingleAttributeList());
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.in("orders.id", shippedOrders);
    }, rob, custNoAddress);

    testQuery(condition -> {
      condition.notIn("orders.id", shippedOrders);
    }, rob, custNoAddress);
  }

  @Test
  public void testAllEq() throws Exception {
    Map<String, Object> map1 = new HashMap<>();
    map1.put("contacts.firstName", "Tracy");
    map1.put("contacts.lastName", "Red");

    Map<String, Object> map2 = new HashMap<>();
    map2.put("contacts.firstName", "Jack");
    map2.put("contacts.lastName", "Black");

    testQuery(condition -> {
      condition.allEq(map1);
    }, fiona);

    testQuery(condition -> {
      condition.or() //
          .and() //
          .eq("contacts.firstName", "Tracy") //
          .eq("contacts.lastName", "Red") //
          .endAnd() //
          .and() //
          .eq("contacts.firstName", "Jack") //
          .eq("contacts.lastName", "Black") //
          .endAnd() //
          .endOr();
    }, custNoAddress, fiona);

    testQuery(condition -> {
      condition.or().allEq(map1).allEq(map2).endOr();
    }, custNoAddress, fiona);
  }

  @Test
  public void testAnd() throws Exception {
    testQuery(condition -> {
      condition.and().eq("contacts.lastName", "Blue").endAnd();
    }, rob, fiona, nocCust);

    testQuery(condition -> {
      condition.and() //
          .eq("contacts.firstName", "Fred1") //
          .endAnd();
    }, rob, fiona, nocCust);

    testQuery(condition -> {
      condition.and() //
          .eq("contacts.firstName", "Fred1") //
          .eq("contacts.lastName", "Blue") //
          .endAnd();
    }, rob, fiona, nocCust);

    testQuery(condition -> {
      condition.and() //
          .eq("contacts.firstName", "Fred1") //
          .eq("contacts.firstName", "Bugs1") //
          .endAnd();
    });

  }

  @Test
  public void testOr() throws Exception {
    testQuery(condition -> {
      condition.or() //
          .eq("contacts.lastName", "Blue") //
          .endOr();
    }, rob, fiona, nocCust);

    testQuery(condition -> {
      condition.or() //
          .eq("contacts.firstName", "Fred1") //
          .endOr();
    }, rob, fiona, nocCust);

    testQuery(condition -> {
      condition.or() //
          .eq("contacts.firstName", "Fred1") //
          .eq("contacts.lastName", "Blue") //
          .endOr();
    }, rob, fiona, nocCust);

    testQuery(condition -> {
      condition.or() //
          .eq("contacts.firstName", "Fred1") //
          .eq("contacts.firstName", "Bugs1") //
          .endOr();
    }, rob, fiona, nocCust);

    testQuery(condition -> {
      condition.or() //
          .eq("contacts.firstName", "Does") //
          .eq("contacts.firstName", "Not") //
          .eq("contacts.firstName", "Exist") //
          .endOr();
    });

    testQuery(condition -> {
      condition.or() //
          .eq("orders.details.product.name", "Desk") //
          .eq("orders.details.product.name", "Chair") //
          .endOr();
    }, rob, custNoAddress);
  }

  @Test
  public void testComplex() throws Exception {
    testQuery(condition -> {
      condition.or() //
          .eq("orders.details.product.name", "x1") //
          .eq("orders.details.product.name", "x2") //
//          .eq("orders.shipments.id", 15) //
          .eq("contacts.firstName", "x3") //
          .eq("contacts.firstName", "x4") //
          .endOr();
    });
  }

  @Test
  public void testNot() throws Exception {
    testQuery(condition -> {
      condition.not() //
          .eq("billingAddress.line1", "P.O.Box 1234") //
          .eq("billingAddress.line1", "West Coast Rd") //
          .endNot();
    }, rob, fiona, nocCust);

    testQuery(condition -> {
      condition.not() //
          .eq("billingAddress.line1", "West Coast Rd") //
          .endNot();
    }, rob, nocCust);

    testQuery(condition -> {
      condition.not() //
          .eq("billingAddress.line1", "P.O.Box 1234") //
          .endNot();
    }, fiona, nocCust);

  }

  private <T> void testQuery(Consumer<QueryDsl<Customer,?>> condition, Customer... expected) {

    // Query
    Query<Customer> query = Ebean.find(Customer.class);
    condition.accept(query.where());

    Query<Customer> copy = query.copy();
    //copy.where().le("id", 4); // limit to builtin
    List<Customer> ref = copy.order("id").findList();
    assertThat(ref).containsExactly(expected);

    Filter<Customer> filter = Ebean.filter(Customer.class);
    condition.accept(filter);

    // now search all, and filter them manually
    List<Customer> all = Arrays.asList(rob, custNoAddress, fiona, nocCust);
    List<Customer> filtered = filter.filter(all);

    // System.out.println(query.getQueryPlanKey());
    assertThat(filtered).containsExactly(expected);

    // now test "applyTo
    filter = Ebean.filter(Customer.class);
    query.where().applyTo(filter);

    filtered = filter.filter(all);

    // System.out.println(query.getQueryPlanKey());
    assertThat(filtered).containsExactly(expected);

  }

}
