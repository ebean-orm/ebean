package com.avaje.ebean.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DbConstraintNamingTest {

  DbConstraintNaming naming = new DbConstraintNaming();

  @Test
  public void testPrimaryKeyName() throws Exception {

    assertThat(naming.primaryKeyName("[cat].[sce].[foo_bar]")).isEqualTo("pk_foo_bar");
  }

  @Test
  public void testUniqueConstraintName() throws Exception {

    assertThat(naming.uniqueConstraintName("[foo_bar]", "[jim]")).isEqualTo("uq_foo_bar_jim");
  }

  @Test
  public void testCheckConstraintName() throws Exception {

    assertThat(naming.checkConstraintName("[foo_bar]", "[jim]")).isEqualTo("ck_foo_bar_jim");
  }

  @Test
  public void testNormalise() throws Exception {

    assertThat(naming.normaliseTable("cat.sch.foo_bar]")).isEqualTo("foo_bar");
    assertThat(naming.normaliseTable("sch.foo_bar]")).isEqualTo("foo_bar");
    assertThat(naming.normaliseTable("foo_bar]")).isEqualTo("foo_bar");
  }

}