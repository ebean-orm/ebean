package org.tests.inheritance.abstrakt;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

public class TestInheritanceNum extends BaseTestCase {

  @Test
  public void basicIUD() {

    Block block = new Block();
    block.setName("ibe");
    block.setNotes("try it");

    DB.save(block);
  }
}
