package io.ebean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlRowBooleanTest extends BaseTestCase {

  @Test
  public void getBoolean() {

    SqlQuery sqlQuery;
    if (isSqlServer()) {
      sqlQuery = DB.sqlQuery("SELECT 1 AS ISNT_NULL");
    } else if (isOracle() || isNuoDb()) {
      sqlQuery = DB.sqlQuery("SELECT 1 AS ISNT_NULL from dual");
    } else if (isDb2() || isDb2ForI()) {
      sqlQuery = DB.sqlQuery("SELECT 1 AS ISNT_NULL from SYSIBM.SYSDUMMY1");
    } else if (isHana()) {
      sqlQuery = DB.sqlQuery("SELECT 1 AS ISNT_NULL from sys.dummy");
    } else {
      sqlQuery = DB.sqlQuery("SELECT 1 IS NOT NULL AS ISNT_NULL");
    }
    SqlRow row = sqlQuery.findOne();
    Boolean value = row.getBoolean("ISNT_NULL");
    assertThat(value).isTrue();
  }
}
