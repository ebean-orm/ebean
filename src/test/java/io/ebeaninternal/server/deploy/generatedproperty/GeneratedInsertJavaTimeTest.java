package io.ebeaninternal.server.deploy.generatedproperty;


import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GeneratedInsertJavaTimeTest {

  @Test
  public void test_generatedOnInsert() {

    GeneratedProperty gen = new GeneratedInsertJavaTime.InstantDT();
    assertTrue(GeneratedWhenCreated.class.isInstance(gen));
    assertTrue(gen instanceof GeneratedWhenCreated);
  }
}
