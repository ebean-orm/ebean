package org.tests.model.selfref;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestTextJsonSelfRef extends BaseTestCase {

  @Test
  public void test() {

    Ebean.execute(() -> {

      if (Ebean.find(SelfRefCustomer.class).findCount() == 0) {
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
    });

    List<SelfRefCustomer> customers = Ebean.find(SelfRefCustomer.class).orderBy("id desc").findList();

    // Check that there are no 'reference' beans here
    for (SelfRefCustomer cust : customers) {
      BeanState beanState = Ebean.getBeanState(cust);
      Assert.assertFalse(beanState.isReference());
    }

//    JsonWriteOptions options = JsonWriteOptions.parsePath("(id,name,referredBy(id))");
//    String customerContent = Ebean.createJsonContext().toJson(customers);//, false, options);
//    System.out.println("Customers: " + customerContent);
//
//    Assert
//        .assertEquals(
//            "[{\"id\":3,\"name\":\"baz\",\"referredBy\":{\"id\":1}},{\"id\":2,\"name\":\"Bar\",\"referredBy\":{\"id\":1}},{\"id\":1,\"name\":\"Foo\",\"referredBy\":{\"id\":1}}]",
//            customerContent);
  }

}
