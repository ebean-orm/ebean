package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.tests.model.embedded.EMain;
import org.junit.Assert;
import org.junit.Test;

public class TestDynamicUpdate extends BaseTestCase {

  @Test
  public void testUpdate() {

    // insert
    EMain b = new EMain();
    b.setName("aaa");
    b.getEmbeddable().setDescription("123");

    EbeanServer server = Ebean.getServer(null);

    server.save(b);

    Assert.assertNotNull(b.getId());

    // reload object und update the name
    EMain b2 = server.find(EMain.class, b.getId());

    b2.getEmbeddable().setDescription("ABC");

    BeanState beanState = server.getBeanState(b2);
    boolean dirty = beanState.isDirty();
    Assert.assertTrue(dirty);

    server.save(b2);

    server.beginTransaction();
    try {
      EMain b3 = server.find(EMain.class, b.getId());
      Assert.assertEquals("ABC", b3.getEmbeddable().getDescription());
    } finally {
      server.endTransaction();
    }
    EMain b4 = server.find(EMain.class, b.getId());
    b4.setName("bbb");
    b4.getEmbeddable().setDescription("123");
    server.save(b4);

    EMain b5 = server.find(EMain.class, b.getId());
    Assert.assertEquals("123", b5.getEmbeddable().getDescription());
  }
}
