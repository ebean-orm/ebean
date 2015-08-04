package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DdlNamingConventionTest {

  DdlNamingConvention defaultNaming = new DdlNamingConvention();

  @Test
  public void testPrimaryKeyName() throws Exception {

    List<String> cols = new ArrayList<String>();
    cols.add("[jim]");
    cols.add("`jack`");
    assertThat(defaultNaming.primaryKeyName("[cat].[sce].[foo_bar]", cols)).isEqualTo("pk_foo_bar");
  }

  @Test
  public void testUniqueConstraintName() throws Exception {

    assertThat(defaultNaming.uniqueConstraintName("[foo_bar]", "[jim]")).isEqualTo("uq_foo_bar_jim");
  }

  @Test
  public void testCheckConstraintName() throws Exception {

    assertThat(defaultNaming.checkConstraintName("[foo_bar]", "[jim]")).isEqualTo("ck_foo_bar_jim");
  }

  @Test
  public void testNormalise() throws Exception {

    assertThat(defaultNaming.normalise("cat.sch.foo_bar]")).isEqualTo("foo_bar");
    assertThat(defaultNaming.normalise("sch.foo_bar]")).isEqualTo("foo_bar");
    assertThat(defaultNaming.normalise("foo_bar]")).isEqualTo("foo_bar");
  }

}