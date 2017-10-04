package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.DbConstraintNormalise;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class DbNameNormaliseTest {

  DbConstraintNormalise normalise = new DbConstraintNormalise();

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
