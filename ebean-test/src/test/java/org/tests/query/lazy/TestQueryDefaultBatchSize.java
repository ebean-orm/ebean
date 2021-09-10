package org.tests.query.lazy;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.ProfileLocation;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.util.List;

public class TestQueryDefaultBatchSize extends BaseTestCase {

  private static final ProfileLocation loc0 = ProfileLocation.create();
  private static final ProfileLocation loc1 = ProfileLocation.create();
  private static final ProfileLocation loc2 = ProfileLocation.create();
  private static final ProfileLocation loc3 = ProfileLocation.create();

  @Test
  public void test_findEach() {

    ResetBasicData.reset();

    DB.find(Order.class)
      .setProfileLocation(loc0)
      .setLazyLoadBatchSize(2)
      .findEach(bean -> doStuff(bean));
  }

  @Test
  public void test_findEach_withFetch() {

    ResetBasicData.reset();

    DB.find(Order.class)
      .setProfileLocation(loc1)
      .fetch("details", "id")
      .fetch("details.product", "sku")
      .fetch("customer")
      .fetch("customer.contacts")
      .setLazyLoadBatchSize(2)
      .findEach(bean -> doStuff(bean));
  }

  @Test
  public void test_findList() {

    ResetBasicData.reset();

    List<Order> orders =
      DB.find(Order.class)
        .setProfileLocation(loc2)
        .setLazyLoadBatchSize(2)
        .findList();

    for (Order order : orders) {
      doStuff(order);
    }
  }

  @Test
  public void test_findList_withFetch() {

    ResetBasicData.reset();

    List<Order> orders =
      DB.find(Order.class)
        .setProfileLocation(loc3)
        .fetch("details", "id")
        .fetch("details.product", "sku")
        .fetch("customer")
        .fetch("customer.contacts")
        .setLazyLoadBatchSize(2)
        .findList();

    for (Order order : orders) {
      doStuff(order);
    }
  }

  @Test
  public void test_findList_lazyMany() {

    ResetBasicData.reset();

    List<Order> orders = DB.find(Order.class)
      .setLazyLoadBatchSize(100)
      .findList();

    for (Order order : orders) {
      List<OrderDetail> details = order.getDetails();
      details.size();
      for (OrderDetail detail : details) {
        Product product = detail.getProduct();
        product.getSku();
      }
    }
  }

  private void doStuff(Order order) {
    // invoke lazy loading
    Customer customer = order.getCustomer();
    order.getId();
    customer.getName();
    customer.getContacts().size();
    order.getDetails().size();
  }
}
