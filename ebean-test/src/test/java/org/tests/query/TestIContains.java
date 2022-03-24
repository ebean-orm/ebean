package org.tests.query;


import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;

public class TestIContains extends BaseTestCase {

  @Test
  public void testIContains() {

    ResetBasicData.reset();

    // case insensitive
    Query<Customer> query = DB.find(Customer.class).where().icontains("name", "Rob").query();

    query.findList();
    String generatedSql = query.getGeneratedSql();

    assertThat(generatedSql).contains("lower(t0.name)");

    // case sensitive
    query = DB.find(Customer.class).where().contains("name", "Rob").query();

    query.findList();
    generatedSql = query.getGeneratedSql();

    assertThat(generatedSql).contains(" t0.name ");

    DB.find(Customer.class).where().icontains("name", "Rob").findList();
    DB.find(Customer.class).where().icontains("name", "Rob").findList();
    DB.find(Customer.class).where().icontains("name", "Rob").findList();

    String sql = "select id, status, name from o_customer where lower(name) like :name";
    RawSql parse = RawSqlBuilder.parse(sql).create();

    DB.find(Customer.class).setRawSql(parse).setParameter("name", "Jim").findList();
  }

}
