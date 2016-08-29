package com.avaje.ebean;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlRowBooleanTest {

  @Test
  public void getBoolean() {

    SqlQuery sqlQuery = Ebean.createSqlQuery("SELECT 1 IS NOT NULL AS `ISNT_NULL`");
    SqlRow row = sqlQuery.findUnique();

    Boolean value = row.getBoolean("ISNT_NULL");

    assertThat(value).isTrue();
  }
}