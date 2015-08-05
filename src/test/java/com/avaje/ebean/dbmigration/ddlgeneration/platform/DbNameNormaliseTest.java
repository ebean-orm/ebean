package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class DbNameNormaliseTest {

  DdlNameNormalise normalise = new DdlNameNormalise();

  @Test
  public void testNormalise() throws Exception {

    assertThat(normalise.normaliseTable("cat.sch.foo_bar]")).isEqualTo("foo_bar");
    assertThat(normalise.normaliseTable("sch.foo_bar]")).isEqualTo("foo_bar");
    assertThat(normalise.normaliseTable("foo_bar]")).isEqualTo("foo_bar");
  }

  @Test
  public void testTrimQuotes() throws Exception {

    assertThat(normalise.trimQuotes("[foo]")).isEqualTo("foo");
    assertThat(normalise.trimQuotes("'foo'")).isEqualTo("foo");
    assertThat(normalise.trimQuotes("\"foo\"")).isEqualTo("foo");
    assertThat(normalise.trimQuotes("`foo`")).isEqualTo("foo");

    assertThat(normalise.trimQuotes("`fo_o`")).isEqualTo("fo_o");
  }
}