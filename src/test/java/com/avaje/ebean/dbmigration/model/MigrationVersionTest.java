package com.avaje.ebean.dbmigration.model;

import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class MigrationVersionTest {



  @Test
  public void test_parse_getComment() throws Exception {

    assertThat(MigrationVersion.parse("1.1.1_2__Foo").getComment()).isEqualTo("Foo");
    assertThat(MigrationVersion.parse("1.1.1.2__junk").getComment()).isEqualTo("junk");
    assertThat(MigrationVersion.parse("1.1_1.2_foo").getComment()).isEqualTo("");
    assertThat(MigrationVersion.parse("1.1_1.2_d").getComment()).isEqualTo("");
    assertThat(MigrationVersion.parse("1.1_1.2_").getComment()).isEqualTo("");
    assertThat(MigrationVersion.parse("1.1_1.2").getComment()).isEqualTo("");
  }

  @Test
  public void test_nextVersion_expect_preserveUnderscores() {

    assertThat(MigrationVersion.parse("2").nextVersion()).isEqualTo("3");
    assertThat(MigrationVersion.parse("1.0").nextVersion()).isEqualTo("1.1");
    assertThat(MigrationVersion.parse("2.0.b34").nextVersion()).isEqualTo("2.1");
    assertThat(MigrationVersion.parse("1.1.1_2__Foo").nextVersion()).isEqualTo("1.1.1_3");
    assertThat(MigrationVersion.parse("1.1.1.2_junk").nextVersion()).isEqualTo("1.1.1.3");
    assertThat(MigrationVersion.parse("1_2.3_4__Foo").nextVersion()).isEqualTo("1_2.3_5");
    assertThat(MigrationVersion.parse("1_2.3_4_").nextVersion()).isEqualTo("1_2.3_5");
    assertThat(MigrationVersion.parse("1_2_3_4__Foo").nextVersion()).isEqualTo("1_2_3_5");
  }

  @Test
  public void test_normalised_expect_periods() {

    assertThat(MigrationVersion.parse("2").normalised()).isEqualTo("2");
    assertThat(MigrationVersion.parse("1.0").normalised()).isEqualTo("1.0");
    assertThat(MigrationVersion.parse("2.0.b34").normalised()).isEqualTo("2.0");
    assertThat(MigrationVersion.parse("1.1.1_2__Foo").normalised()).isEqualTo("1.1.1.2");
    assertThat(MigrationVersion.parse("1.1.1.2_junk").normalised()).isEqualTo("1.1.1.2");
    assertThat(MigrationVersion.parse("1_2.3_4__Foo").normalised()).isEqualTo("1.2.3.4");
    assertThat(MigrationVersion.parse("1_2.3_4_").normalised()).isEqualTo("1.2.3.4");
    assertThat(MigrationVersion.parse("1_2_3_4__Foo").normalised()).isEqualTo("1.2.3.4");
  }

  @Test
  public void test_compareTo_isEqual() throws Exception {

    MigrationVersion v0 = MigrationVersion.parse("1.1.1_2__Foo");
    MigrationVersion v1 = MigrationVersion.parse("1.1.1.2_junk");
    MigrationVersion v2 = MigrationVersion.parse("1.1_1.2_foo");

    assertThat(v0.compareTo(v1)).isEqualTo(0);
    assertThat(v1.compareTo(v0)).isEqualTo(0);
    assertThat(v1.compareTo(v2)).isEqualTo(0);
  }

  @Test
  public void test_compareTo() throws Exception {

    MigrationVersion v0 = MigrationVersion.parse("1.1.1.1_junk");
    MigrationVersion v1 = MigrationVersion.parse("1.1.1.2_junk");
    MigrationVersion v2 = MigrationVersion.parse("1.1_1.3_junk");
    MigrationVersion v3 = MigrationVersion.parse("1.2_1.2_junk");
    MigrationVersion v4 = MigrationVersion.parse("2.1_1.2_junk");

    assertThat(v1.compareTo(v0)).isEqualTo(1);

    assertThat(v1.compareTo(v2)).isEqualTo(-1);
    assertThat(v1.compareTo(v3)).isEqualTo(-1);
    assertThat(v1.compareTo(v4)).isEqualTo(-1);
  }
}