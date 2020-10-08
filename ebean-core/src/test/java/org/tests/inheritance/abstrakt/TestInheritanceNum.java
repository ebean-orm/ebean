package org.tests.inheritance.abstrakt;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

public class TestInheritanceNum extends BaseTestCase {

  @Test
  public void basicIUD() {

    Block block = new Block();
    block.setName("ibe");
    block.setNotes("try it");

    Ebean.save(block);
  }
}
