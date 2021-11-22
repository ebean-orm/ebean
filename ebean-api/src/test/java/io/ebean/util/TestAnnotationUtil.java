package io.ebean.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.ebean.annotation.Formula;

public class TestAnnotationUtil {

  @Formula(select = "x")
  @Formula(select = "y")
  private static class TestObject {

  }

  @Test
  public void testRepeatableAnnotation() {

    Set<Formula> list = AnnotationUtil.typeGetAll(TestObject.class, Formula.class);
    assertThat(list).hasSize(2);

  }

}
