package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.junit.Test;
import org.tests.inheritance.model.GroupConfiguration;
import org.tests.inheritance.model.ProductConfiguration;

import java.util.List;

public class TestInheritanceDiscriminatorQueryCache extends BaseTestCase {

  @Test
  public void testDiscriminatorQueryCache() {
    EbeanServer server = Ebean.getDefaultServer();

    ProductConfiguration pc1 = new ProductConfiguration();
    pc1.setName("PC1");
    server.save(pc1);

    ProductConfiguration pc2 = new ProductConfiguration();
    pc1.setName("PC2");
    server.save(pc2);

    GroupConfiguration gc1 = new GroupConfiguration();
    gc1.setName("GC1");
    server.save(gc1);

    GroupConfiguration gc2 = new GroupConfiguration();
    gc1.setName("GC2");
    server.save(gc2);

    List<ProductConfiguration> list1 = server.createQuery(ProductConfiguration.class).setUseQueryCache(true).findList();
    List<GroupConfiguration> list2 = server.createQuery(GroupConfiguration.class).setUseQueryCache(true).findList();

    for(ProductConfiguration pc : list1) {
      System.out.print(pc.getProductName());
    }
    for(GroupConfiguration gc : list2) {
      System.out.print(gc.getGroupName());
    }
  }

}
