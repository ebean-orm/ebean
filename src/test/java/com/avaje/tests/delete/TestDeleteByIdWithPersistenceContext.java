package com.avaje.tests.delete;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.tests.model.basic.Product;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestDeleteByIdWithPersistenceContext extends BaseTestCase {

  @Test
  public void test() {
    
    ResetBasicData.reset();
    
    EbeanServer server = Ebean.getServer(null);
    Product prod1 = createProduct(100,"apples");
    server.insert(prod1);
    Product prod2 = createProduct(101, "bananas");
    server.insert(prod2);
    
    server.beginTransaction();
    // effectively load these into the persistence context
    server.find(Product.class, prod1.getId());
    server.find(Product.class, prod2.getId());
    
    server.delete(Product.class, Arrays.asList(prod1.getId(), prod2.getId()));
    
    // are these found in the persistence context?
    Product shadow1 = server.find(Product.class, prod1.getId());
    Product shadow2 = server.find(Product.class, prod2.getId());
    
    Assert.assertNull(shadow1);
    Assert.assertNull(shadow2);
    
    server.endTransaction();
    
  }
  
  private Product createProduct(Integer id, String name) {
    Product prod = new Product();
    prod.setId(id);
    prod.setName(name);
    return prod;
  }
  
}
