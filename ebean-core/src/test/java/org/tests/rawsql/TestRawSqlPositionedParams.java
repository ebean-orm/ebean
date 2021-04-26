package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRawSqlPositionedParams extends BaseTestCase {

  private static final RawSql RAWSQL_1 = RawSqlBuilder
    .parse("select r.id, r.name from o_customer r where r.id >= ? and r.name like ?")
    .create();

  private static final RawSql RAW_SQL_2 = RawSqlBuilder
    .unparsed("select r.id, r.name from o_customer r where r.id >= ? and r.name like ?")
    .columnMapping("r.id", "id")
    .columnMapping("r.name", "name")
    .create();

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class)
      .setRawSql(RAWSQL_1)
      .setParameter(1)
      .setParameter("R%")
      .where().lt("id", 2001)
      .findList();

    assertThat(list).isNotNull();
  }

  @Test
  public void test_unparsed() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class)
      .setRawSql(RAW_SQL_2)
      .setParameter(1)
      .setParameter("R%")
      .findList();

    assertThat(list).isNotNull();
  }
}
