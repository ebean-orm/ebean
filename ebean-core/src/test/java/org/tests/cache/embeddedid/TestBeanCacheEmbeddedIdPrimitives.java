package org.tests.cache.embeddedid;

import io.ebean.Ebean;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class TestBeanCacheEmbeddedIdPrimitives {

  @Test
  public void findById() {

    CEPProduct product1a = new CEPProduct("Product 01");
    Ebean.save(product1a);

    assertNotNull(product1a.getId());

    CEPCategory category1a = new CEPCategory("Category 01");
    Ebean.save(category1a);

    assertNotNull(category1a.getId());

    CEPProduct product1 = Ebean.find(CEPProduct.class).setUseCache(true).where().eq("id", product1a.getId()).findOne();

    assertNotNull(product1);

    product1.getProductCategories().forEach(customerAddress -> {
      System.out.println(customerAddress.getCategory());
    });
    assertThat(product1.getProductCategories()).isEmpty();

    CEPProduct product2 = Ebean.find(CEPProduct.class).setUseCache(true).where().eq("id", product1a.getId()).findOne();
    assertNotNull(product2);

    // fail here on null -> long conversion in embedded id
    product2.getProductCategories().forEach(customerAddress -> {
      System.out.println(customerAddress.getCategory());
    });
    assertThat(product2.getProductCategories()).isEmpty();
  }


}
