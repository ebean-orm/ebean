package io.ebeaninternal.server.rawsql;

import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebeaninternal.server.rawsql.SpiRawSql.Sql;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestRawSqlBuilderDistinct {

  @Test
  public void testDistinct() {

    RawSql r = RawSqlBuilder.parse("select distinct id, name from t_cust").create();
    Sql sql = ((SpiRawSql)r).getSql();
    assertEquals("id, name", sql.getPreFrom());
    assertEquals("from t_cust", sql.getPreWhere());
    assertEquals("", sql.getPreHaving());
    assertNull(sql.getOrderBy());
  }

}
