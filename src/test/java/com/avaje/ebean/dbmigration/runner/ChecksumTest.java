package com.avaje.ebean.dbmigration.runner;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChecksumTest {

  @Test
  public void test_calculate() throws Exception {

    int checkFoo = Checksum.calculate("foo");

    assertThat(Checksum.calculate("foo")).isEqualTo(checkFoo);
    assertThat(Checksum.calculate("Foo")).isNotEqualTo(checkFoo);
  }

}