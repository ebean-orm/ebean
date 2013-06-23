package com.avaje.tests.model.selfref;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.TxRunnable;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.tests.model.selfref.SelfRefCustomer;

public class TestTextJsonSelfRef extends BaseTestCase {

  @Test
  public void test() {

    Ebean.execute(new TxRunnable() {
      public void run() {

        if (Ebean.find(SelfRefCustomer.class).findRowCount() == 0) {
          SelfRefCustomer c1 = new SelfRefCustomer();
          c1.setName("Foo");
          c1.setReferredBy(c1);

          SelfRefCustomer c2 = new SelfRefCustomer();
          c2.setName("Bar");
          c2.setReferredBy(c1);

          SelfRefCustomer c3 = new SelfRefCustomer();
          c3.setName("baz");
          c3.setReferredBy(c1);

          Ebean.save(c1);
          Ebean.save(c2);
          Ebean.save(c3);
        }
      }
    });

    List<SelfRefCustomer> customers = Ebean.find(SelfRefCustomer.class).orderBy("id desc").findList();
    
    // Check that there are no 'reference' beans here
    for (SelfRefCustomer cust: customers) {
      BeanState beanState = Ebean.getBeanState(cust);
      Assert.assertFalse(beanState.isReference());
    }
    
//    JsonWriteOptions options = JsonWriteOptions.parsePath("(id,name,referredBy(id))");
//    String customerContent = Ebean.createJsonContext().toJsonString(customers);//, false, options);
//    System.out.println("Customers: " + customerContent);
//
//    Assert
//        .assertEquals(
//            "[{\"id\":3,\"name\":\"baz\",\"referredBy\":{\"id\":1}},{\"id\":2,\"name\":\"Bar\",\"referredBy\":{\"id\":1}},{\"id\":1,\"name\":\"Foo\",\"referredBy\":{\"id\":1}}]",
//            customerContent);
  }

}