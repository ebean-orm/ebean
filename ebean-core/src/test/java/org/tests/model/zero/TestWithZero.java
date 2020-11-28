package org.tests.model.zero;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestWithZero extends BaseTestCase {

  @Test
  public void testInsertUpdate() {


    WithZero withZero = new WithZero();
    assertEquals(0, withZero.getId());
    assertEquals(0, withZero.getVersion());
    assertNull(withZero.getName());

    WithZeroParent parent = new WithZeroParent();
    parent.getChildren().add(withZero);

    Ebean.save(parent);

    parent.getChildren().add(new WithZero());

    //withZero.setName("Foo");
    Ebean.save(parent);

  }


}
