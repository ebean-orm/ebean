package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.Database;
import org.tests.model.embedded.EMain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestDynamicUpdate extends BaseTestCase {

  @Test
  public void testUpdate() {

    // insert
    EMain b = new EMain();
    b.setName("aaa");
    b.getEmbeddable().setDescription("123");

    Database server = DB.getDefault();

    server.save(b);

    assertNotNull(b.getId());

    // reload object und update the name
    EMain b2 = server.find(EMain.class, b.getId());

    b2.getEmbeddable().setDescription("ABC");

    BeanState beanState = server.beanState(b2);
    boolean dirty = beanState.isDirty();
    assertTrue(dirty);

    server.save(b2);

    server.beginTransaction();
    try {
      EMain b3 = server.find(EMain.class, b.getId());
      assertEquals("ABC", b3.getEmbeddable().getDescription());
    } finally {
      server.endTransaction();
    }
    EMain b4 = server.find(EMain.class, b.getId());
    b4.setName("bbb");
    b4.getEmbeddable().setDescription("123");
    server.save(b4);

    EMain b5 = server.find(EMain.class, b.getId());
    assertEquals("123", b5.getEmbeddable().getDescription());
  }
}
