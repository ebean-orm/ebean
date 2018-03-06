package org.tests.delete;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TestDeleteByIdWithPersistenceContext extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Ebean.delete(Product.class, 100);
    Ebean.delete(Product.class, 101);

    EbeanServer server = Ebean.getServer(null);
    Product prod1 = createProduct(100, "apples");
    server.insert(prod1);
    Product prod2 = createProduct(101, "bananas");
    server.insert(prod2);

    try (Transaction txn = server.beginTransaction()) {
      // effectively load these into the persistence context
      server.find(Product.class, prod1.getId());
      server.find(Product.class, prod2.getId());

      server.deleteAll(Product.class, Arrays.asList(prod1.getId(), prod2.getId()));

      // are these found in the persistence context?
      Product shadow1 = server.find(Product.class, prod1.getId());
      Product shadow2 = server.find(Product.class, prod2.getId());

      Assert.assertNull(shadow1);
      Assert.assertNull(shadow2);

    }

  }

  private Product createProduct(Integer id, String name) {
    Product prod = new Product();
    prod.setId(id);
    prod.setName(name);
    return prod;
  }

}
