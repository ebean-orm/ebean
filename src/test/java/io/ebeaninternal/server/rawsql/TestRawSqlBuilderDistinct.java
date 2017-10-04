package io.ebeaninternal.server.rawsql;

import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebeaninternal.server.rawsql.SpiRawSql.Sql;
import junit.framework.TestCase;
import org.junit.Assert;

public class TestRawSqlBuilderDistinct extends TestCase {

  public void testDistinct() {

    RawSql r = RawSqlBuilder.parse("select distinct id, name from t_cust").create();
    Sql sql = ((SpiRawSql)r).getSql();
    Assert.assertEquals("id, name", sql.getPreFrom());
    Assert.assertEquals("from t_cust", sql.getPreWhere());
    Assert.assertEquals("", sql.getPreHaving());
    Assert.assertNull(sql.getOrderBy());

  }

}
