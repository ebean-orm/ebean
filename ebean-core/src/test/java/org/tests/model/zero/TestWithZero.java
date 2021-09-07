package org.tests.model.zero;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestWithZero extends BaseTestCase {

  @Test
  public void testInsertUpdate() {


    WithZero withZero = new WithZero();
    assertEquals(0, withZero.getId());
    assertEquals(0, withZero.getVersion());
    assertNull(withZero.getName());

    WithZeroParent parent = new WithZeroParent();
    parent.getChildren().add(withZero);

    DB.save(parent);

    parent.getChildren().add(new WithZero());

    //withZero.setName("Foo");
    DB.save(parent);

  }


}
