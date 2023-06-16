package io.ebean.xtest.config;

import io.ebean.config.DbConstraintNaming;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DbConstraintNamingTest {

  DbConstraintNaming naming = new DbConstraintNaming();

  @Test
  public void testPrimaryKeyName() {
    assertThat(naming.primaryKeyName("[cat].[sce].[foo_bar]")).isEqualTo("pk_foo_bar");
  }

  @Test
  public void testUniqueConstraintName() {
    assertThat(naming.uniqueConstraintName("[foo_bar]", "[jim]")).isEqualTo("uq_foo_bar_jim");
  }

  @Test
  public void testCheckConstraintName() {
    assertThat(naming.checkConstraintName("[foo_bar]", "[jim]")).isEqualTo("ck_foo_bar_jim");
  }

  @Test
  public void testNormalise() {
    assertThat(naming.normaliseTable("cat.sch.foo_bar]")).isEqualTo("foo_bar");
    assertThat(naming.normaliseTable("sch.foo_bar]")).isEqualTo("foo_bar");
    assertThat(naming.normaliseTable("foo_bar]")).isEqualTo("foo_bar");
  }

  @Test
  public void testIndexNameWithSpaces() {
    assertThat(naming.indexName("foo", new String[]{"name", "other desc"})).isEqualTo("ix_foo_name_other_desc");
  }

  @Test
  public void testDefaultToLower() {
    assertThat(naming.normaliseTable("SCH.FOO_BAR]")).isEqualTo("foo_bar");
  }

  @Test
  public void testNoLowerCaseTable() {
    DbConstraintNaming naming = new DbConstraintNaming(false, true);
    assertThat(naming.normaliseTable("SCH.FOO_BAR]")).isEqualTo("FOO_BAR");
    assertThat(naming.normaliseColumn("SCH.FOO_BAR]")).isEqualTo("sch.foo_bar");
    // table name not lowered
  }

  @Test
  public void testNoLowerCaseColumn() {
    DbConstraintNaming naming = new DbConstraintNaming(true, false);
    assertThat(naming.normaliseTable("SCH.FOO_BAR]")).isEqualTo("foo_bar");
    assertThat(naming.normaliseColumn("SCH.FOO_BAR]")).isEqualTo("SCH.FOO_BAR");
    // column name not lowered
  }

  @Test
  public void normaliseColumn_withFormula() {
    assertThat(naming.normaliseColumn("lower(name)")).isEqualTo("lowername");
  }
}
