package org.tests.rawsql;

import io.ebean.*;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestRawSqlAsKeyword extends BaseTestCase {

  @Test
  public void test() {

    // Make this false to run this test ... as the pipe string concatenation syntax is DB specific
    boolean skipTestAsDBSpecficSQL = true;

    if (skipTestAsDBSpecficSQL) {

      return;
    }

    ResetBasicData.reset();


    // try valid query where spaces in the formula ...
    RawSql rawSql =
      RawSqlBuilder
        .parse("select r.id, r.name || 'hello' as name from o_customer r ")
        .create();

    Query<Customer> query = DB.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    List<Customer> list = query.findList();
    assertNotNull(list);

    // try valid query with no spaces
    rawSql =
      RawSqlBuilder
        .parse("select r.id, r.name||'hello' as name from o_customer r ")
        .create();

    query = DB.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    list = query.findList();
    assertNotNull(list);

    rawSql =
      RawSqlBuilder
        .parse("select r.id, r.name||'hello' name from o_customer r ")
        .create();
    query = DB.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    list = query.findList();
    assertNotNull(list);

    // this will barf - expecting the AS keyword now
    rawSql =
      RawSqlBuilder
        .parse("select r.id, r.name || 'hello' name from o_customer r ")
        .create();
    query = DB.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    list = query.findList();
    assertNotNull(list);
  }
}
