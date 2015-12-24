package com.avaje.ebean.dbmigration.model;

import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class MigrationVersionTest {

  @Test
  public void testParse() throws Exception {

    MigrationVersion v0 = MigrationVersion.parse("1.1.1_2__Foo");
    MigrationVersion v1 = MigrationVersion.parse("1.1.1.2_junk");
    MigrationVersion v2 = MigrationVersion.parse("1.1_1.2_foo");

    assertThat(v0.compareTo(v1)).isEqualTo(0);
    assertThat(v1.compareTo(v0)).isEqualTo(0);
    assertThat(v1.compareTo(v2)).isEqualTo(0);

  }

  @Test
  public void testNextVersion() {

    assertThat(MigrationVersion.parse("2").nextVersion()).isEqualTo("3");
    assertThat(MigrationVersion.parse("1.0").nextVersion()).isEqualTo("1.1");
    assertThat(MigrationVersion.parse("2.0.b34").nextVersion()).isEqualTo("2.1");
    assertThat(MigrationVersion.parse("1.1.1_2__Foo").nextVersion()).isEqualTo("1.1.1.3");
    assertThat(MigrationVersion.parse("1.1.1.2_junk").nextVersion()).isEqualTo("1.1.1.3");
  }

  @Test
  public void testCompareTo() throws Exception {

    MigrationVersion v1 = MigrationVersion.parse("1.1.1.2_junk");
    MigrationVersion v2 = MigrationVersion.parse("2.1_1.2_junk");
    MigrationVersion v3 = MigrationVersion.parse("1.2_1.2_junk");
    MigrationVersion v4 = MigrationVersion.parse("1.1_1.3_junk");
    MigrationVersion v5 = MigrationVersion.parse("1.1.1.1_junk");

    assertThat(v1.compareTo(v2)).isEqualTo(-1);
    assertThat(v1.compareTo(v3)).isEqualTo(-1);
    assertThat(v1.compareTo(v4)).isEqualTo(-1);

    assertThat(v1.compareTo(v5)).isEqualTo(1);
  }
}