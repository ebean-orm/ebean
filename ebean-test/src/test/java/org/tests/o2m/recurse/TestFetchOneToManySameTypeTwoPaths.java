package org.tests.o2m.recurse;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.FetchConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestFetchOneToManySameTypeTwoPaths {

  @Test
  void testSubItemListFetch_ofQuery() {
    Database server = DB.getDefault();

    RMItem itemA = new RMItem("a");
    server.save(itemA);
    RMItem itemB = new RMItem("b");
    server.save(itemB);

    for (int i=0; i<2; i++) {
      RMItem subItem = new RMItem();
      subItem.setItemGroup(itemA);
      server.save(subItem);
    }

    for (int i=0; i<3; i++) {
      RMItem subItem = new RMItem();
      subItem.setItemGroup(itemB);
      server.save(subItem);
    }

    RMItemHolder customer = new RMItemHolder();
    customer.setItemA(itemA);
    customer.setItemB(itemB);
    server.save(customer);

    // This is OK
    {
      RMItemHolder requestedCustomer = server.find(RMItemHolder.class)
        .setDisableLazyLoading(true)
        .fetch("itemA.subItems", FetchConfig.ofQuery())
        .fetch("itemB.subItems", FetchConfig.ofQuery())
        .where()
        .eq("id", customer.getId())
        .findOne();
      assertEquals(2, requestedCustomer.getItemA().getSubItems().size());
      assertEquals(3, requestedCustomer.getItemB().getSubItems().size());
    }
  }

  @Test
  void testSubItemListFetch_itemAFirst() {
    Database server = DB.getDefault();

    RMItem itemA = new RMItem("aa");
    server.save(itemA);
    RMItem itemB = new RMItem("bb");
    server.save(itemB);

    for (int i=0; i<2; i++) {
      RMItem subItem = new RMItem();
      subItem.setItemGroup(itemA);
      server.save(subItem);
    }

    for (int i=0; i<3; i++) {
      RMItem subItem = new RMItem();
      subItem.setItemGroup(itemB);
      server.save(subItem);
    }

    RMItemHolder customer = new RMItemHolder();
    customer.setItemA(itemA);
    customer.setItemB(itemB);
    server.save(customer);

    // This fails because requestedCustomer.getItemB().getSubItems() is not loaded
    {
      RMItemHolder requestedCustomer = server.find(RMItemHolder.class)
        .setDisableLazyLoading(true)
        .fetch("itemA.subItems")
        .fetch("itemB.subItems")
        .where()
        .eq("id", customer.getId())
        .findOne();
      assertEquals(2, requestedCustomer.getItemA().getSubItems().size());
      assertEquals(3, requestedCustomer.getItemB().getSubItems().size());
    }
  }

  @Test
  void testSubItemListFetch_itemBFirst() {
    Database server = DB.getDefault();

    RMItem itemA = new RMItem("a");
    server.save(itemA);
    RMItem itemB = new RMItem("b");
    server.save(itemB);

    for (int i=0; i<2; i++) {
      RMItem subItem = new RMItem();
      subItem.setItemGroup(itemA);
      server.save(subItem);
    }

    for (int i=0; i<5; i++) {
      RMItem subItem = new RMItem();
      subItem.setItemGroup(itemB);
      server.save(subItem);
    }

    RMItemHolder customer = new RMItemHolder();
    customer.setItemA(itemA);
    customer.setItemB(itemB);
    server.save(customer);

    // This fails because requestedCustomer.getItemA().getSubItems() is not loaded
    {
      RMItemHolder requestedCustomer = server.find(RMItemHolder.class)
        .setDisableLazyLoading(true)
        .fetch("itemB.subItems")
        .fetch("itemA.subItems")
        .where()
        .eq("id", customer.getId())
        .findOne();
      assertEquals(2, requestedCustomer.getItemA().getSubItems().size());
      assertEquals(5, requestedCustomer.getItemB().getSubItems().size());
      System.out.println("here");
    }
  }

}
