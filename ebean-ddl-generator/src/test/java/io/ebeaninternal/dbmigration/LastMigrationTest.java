package io.ebeaninternal.dbmigration;

import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class LastMigrationTest {

  @Test
  public void lastVersion() {
    File d = new File("src/test/resources/dbmigration/migrationtest/h2");
    assertThat(LastMigration.lastVersion(d, null)).isEqualTo("1.4");
    assertThat(LastMigration.nextVersion(d, null, false)).isEqualTo("1.5");
    assertThat(LastMigration.nextVersion(d, null, true)).isEqualTo("1.4");
  }

  @Test
  public void lastVersion_no_v_Prefix() {
    File d = new File("src/test/resources/dbmigration/migrationtest-history");
    assertThat(LastMigration.lastVersion(d, null)).isEqualTo("1.2");
  }

  @Test
  public void lastVersion_test() {
    File d = new File("src/test/resources/test-dbmigration");
    assertThat(LastMigration.lastVersion(d, null)).isEqualTo("2.1");
    assertThat(LastMigration.nextVersion(d, null, false)).isEqualTo("2.2");

  }

  @Test
  public void lastVersion_app2() {
    File d = new File("src/test/resources/dbmigration/app2");
    assertThat(LastMigration.lastVersion(d, null)).isEqualTo("3.1");
    assertThat(LastMigration.nextVersion(d, null, false)).isEqualTo("3.2");
  }

  @Test
  public void lastVersion_app3() {
    File d = new File("src/test/resources/dbmigration/app3");
    assertThat(LastMigration.lastVersion(d, null)).isEqualTo("3.1.2");
    assertThat(LastMigration.nextVersion(d, null, false)).isEqualTo("3.1.3");
    assertThat(LastMigration.nextVersion(d, null, true)).isEqualTo("3.1.2");
  }

  @Test
  public void lastVersion_app3_with_model_larger() {
    File d = new File("src/test/resources/dbmigration/app3");
    File m = new File("src/test/resources/dbmigration/app3/model_larger");
    assertThat(LastMigration.lastVersion(d, m)).isEqualTo("4.1");
    assertThat(LastMigration.nextVersion(d, m, false)).isEqualTo("4.2");
    assertThat(LastMigration.nextVersion(d, m, true)).isEqualTo("4.1");
  }

  @Test
  public void lastVersion_app3_with_model_smaller() {

    File d = new File("src/test/resources/dbmigration/app3");
    File m = new File("src/test/resources/dbmigration/app3/model_smaller");
    assertThat(LastMigration.lastVersion(d, m)).isEqualTo("3.1.2");
    assertThat(LastMigration.nextVersion(d, m, false)).isEqualTo("3.1.3");
    assertThat(LastMigration.nextVersion(d, m, true)).isEqualTo("3.1.2");
  }
}
