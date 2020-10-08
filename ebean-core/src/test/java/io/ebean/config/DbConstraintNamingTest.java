package io.ebean.config;

import org.junit.Test;

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
  public void testDefaultToLower() {
    assertThat(naming.normaliseTable("SCH.FOO_BAR]")).isEqualTo("foo_bar");
    assertThat(naming.lowerTableName("SCH.FOO_BAR]")).isEqualTo("SCH.FOO_BAR]");
    assertThat(naming.lowerTableName("SCH.FOO_BAR")).isEqualTo("sch.foo_bar");
    assertThat(naming.lowerColumnName("SCH.FOO_BAR]")).isEqualTo("SCH.FOO_BAR]");
    assertThat(naming.lowerColumnName("SCH.FOO_BAR")).isEqualTo("sch.foo_bar");
  }

  @Test
  public void testNoLowerCaseTable() {
    DbConstraintNaming naming = new DbConstraintNaming(false, true);
    assertThat(naming.normaliseTable("SCH.FOO_BAR]")).isEqualTo("FOO_BAR");
    assertThat(naming.normaliseColumn("SCH.FOO_BAR]")).isEqualTo("sch.foo_bar");
    assertThat(naming.lowerTableName("SCH.FOO_BAR]")).isEqualTo("SCH.FOO_BAR]");
    // table name not lowered
    assertThat(naming.lowerTableName("SCH.FOO_BAR")).isEqualTo("SCH.FOO_BAR");
    assertThat(naming.lowerColumnName("SCH.FOO_BAR]")).isEqualTo("SCH.FOO_BAR]");
    assertThat(naming.lowerColumnName("SCH.FOO_BAR")).isEqualTo("sch.foo_bar");
  }

  @Test
  public void testNoLowerCaseColumn() {
    DbConstraintNaming naming = new DbConstraintNaming(true, false);
    assertThat(naming.normaliseTable("SCH.FOO_BAR]")).isEqualTo("foo_bar");
    assertThat(naming.normaliseColumn("SCH.FOO_BAR]")).isEqualTo("SCH.FOO_BAR");
    assertThat(naming.lowerTableName("SCH.FOO_BAR]")).isEqualTo("SCH.FOO_BAR]");
    assertThat(naming.lowerTableName("SCH.FOO_BAR")).isEqualTo("sch.foo_bar");
    assertThat(naming.lowerColumnName("SCH.FOO_BAR]")).isEqualTo("SCH.FOO_BAR]");
    // column name not lowered
    assertThat(naming.lowerColumnName("SCH.FOO_BAR")).isEqualTo("SCH.FOO_BAR");
  }

  @Test
  public void normaliseColumn_withFormula() {
    assertThat(naming.normaliseColumn("lower(name)")).isEqualTo("lowername");
  }
}
