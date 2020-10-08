package org.tests.genkey;

import io.ebean.Ebean;
import org.tests.model.basic.TOne;
import junit.framework.TestCase;
import org.junit.Assert;

public class TestGeneratedKeys extends TestCase {


  public void testJdbcBatchInsert() {

    TOne c = new TOne();
    c.setName("Banana");
    c.setDescription("Test Gen Key");

    TOne c1 = new TOne();
    c1.setName("Two");
    c1.setDescription("Test Gen Key Two");

    Ebean.beginTransaction();
    try {
      Ebean.save(c);
      Ebean.save(c1);
    } finally {
      Ebean.commitTransaction();
    }
    Integer id = c.getId();
    Assert.assertNotNull("Get Id back after insert", id);

    Integer id1 = c1.getId();
    Assert.assertNotNull("Get Id back after insert", id1);

  }

}
