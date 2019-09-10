package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.ConstraintMode;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Oracle10DdlTest {

  Oracle10Ddl create() {
    return new Oracle10Ddl(new OraclePlatform());
  }

  @Test
  public void appendForeignKeyOnDelete_expectEmtpy_when_nullRestrictSetDefault() {

    Oracle10Ddl oracle = create();

    StringBuilder sb = new StringBuilder();
    oracle.appendForeignKeyOnDelete(sb, oracle.withDefault(null));
    assertThat(sb.toString()).isEqualTo("");

    sb = new StringBuilder();
    oracle.appendForeignKeyOnDelete(sb, ConstraintMode.RESTRICT);
    assertThat(sb.toString()).isEqualTo("");


    sb = new StringBuilder();
    oracle.appendForeignKeyOnDelete(sb, ConstraintMode.SET_DEFAULT);
    assertThat(sb.toString()).isEqualTo("");
  }

  @Test
  public void appendForeignKeyOnDelete_setNull() {

    Oracle10Ddl oracle = create();

    StringBuilder sb = new StringBuilder();
    oracle.appendForeignKeyOnDelete(sb, ConstraintMode.SET_NULL);
    assertThat(sb.toString()).isEqualTo(" on delete set null");
  }

  @Test
  public void appendForeignKeyOnDelete_cascade() {

    Oracle10Ddl oracle = create();

    StringBuilder sb = new StringBuilder();
    oracle.appendForeignKeyOnDelete(sb, ConstraintMode.CASCADE);
    assertThat(sb.toString()).isEqualTo(" on delete cascade");
  }

}
