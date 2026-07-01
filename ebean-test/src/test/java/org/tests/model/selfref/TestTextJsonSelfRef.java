package org.tests.model.selfref;

import io.ebean.xtest.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestTextJsonSelfRef extends BaseTestCase {

  @Test
  public void test() {

    DB.execute(() -> {

      if (DB.find(SelfRefCustomer.class).findCount() == 0) {
        SelfRefCustomer c1 = new SelfRefCustomer();
        c1.setName("Foo");
        c1.setReferredBy(c1);

        SelfRefCustomer c2 = new SelfRefCustomer();
        c2.setName("Bar");
        c2.setReferredBy(c1);

        SelfRefCustomer c3 = new SelfRefCustomer();
        c3.setName("baz");
        c3.setReferredBy(c1);

        DB.save(c1);
        DB.save(c2);
        DB.save(c3);
      }
    });

    List<SelfRefCustomer> customers = DB.find(SelfRefCustomer.class).orderBy("id desc").findList();

    // Check that there are no 'reference' beans here
    for (SelfRefCustomer cust : customers) {
      BeanState beanState = DB.beanState(cust);
      assertFalse(beanState.isReference());
    }

//    JsonWriteOptions options = JsonWriteOptions.parsePath("(id,name,referredBy(id))");
//    String customerContent = DB.json().toJson(customers);//, false, options);
//    System.out.println("Customers: " + customerContent);
//
//    Assert
//        .assertEquals(
//            "[{\"id\":3,\"name\":\"baz\",\"referredBy\":{\"id\":1}},{\"id\":2,\"name\":\"Bar\",\"referredBy\":{\"id\":1}},{\"id\":1,\"name\":\"Foo\",\"referredBy\":{\"id\":1}}]",
//            customerContent);
  }

}
