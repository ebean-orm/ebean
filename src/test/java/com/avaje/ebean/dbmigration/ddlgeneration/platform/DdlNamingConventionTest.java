package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DdlNamingConventionTest {

  DdlNamingConvention defaultNaming = new DdlNamingConvention();

  @Test
  public void testPrimaryKeyName() throws Exception {

    assertThat(defaultNaming.primaryKeyName("[cat].[sce].[foo_bar]")).isEqualTo("pk_foo_bar");
  }

  @Test
  public void testUniqueConstraintName() throws Exception {

    assertThat(defaultNaming.uniqueConstraintName("[foo_bar]", "[jim]", 1)).isEqualTo("uq_foo_bar_jim");
  }

  @Test
  public void testCheckConstraintName() throws Exception {

    assertThat(defaultNaming.checkConstraintName("[foo_bar]", "[jim]", 1)).isEqualTo("ck_foo_bar_jim");
  }

  @Test
  public void testNormalise() throws Exception {

    assertThat(defaultNaming.normaliseTable("cat.sch.foo_bar]")).isEqualTo("foo_bar");
    assertThat(defaultNaming.normaliseTable("sch.foo_bar]")).isEqualTo("foo_bar");
    assertThat(defaultNaming.normaliseTable("foo_bar]")).isEqualTo("foo_bar");
  }

}