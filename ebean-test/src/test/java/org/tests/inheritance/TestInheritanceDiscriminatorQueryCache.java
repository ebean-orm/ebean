package org.tests.inheritance;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.Test;
import org.tests.inheritance.model.Configuration;
import org.tests.inheritance.model.Configuration;

import java.util.List;

public class TestInheritanceDiscriminatorQueryCache extends BaseTestCase {

  @Test
  public void testDiscriminatorQueryCache() {
    Database server = DB.getDefault();

    Configuration pc1 = new Configuration();
    pc1.setName("PC1");
    server.save(pc1);

    Configuration pc2 = new Configuration();
    pc1.setName("PC2");
    server.save(pc2);

    Configuration gc1 = new Configuration();
    gc1.setName("GC1");
    server.save(gc1);

    Configuration gc2 = new Configuration();
    gc1.setName("GC2");
    server.save(gc2);

    List<Configuration> list1 = server.createQuery(Configuration.class).setUseQueryCache(true).findList();
    List<Configuration> list2 = server.createQuery(Configuration.class).setUseQueryCache(true).findList();

    for(Configuration pc : list1) {
      System.out.print(pc.getProductName());
    }
    for(Configuration gc : list2) {
      System.out.print(gc.getGroupName());
    }
  }

}
