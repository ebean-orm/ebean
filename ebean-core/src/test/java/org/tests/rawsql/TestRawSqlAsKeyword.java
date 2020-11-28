package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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

    Query<Customer> query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    List<Customer> list = query.findList();
    Assert.assertNotNull(list);


    // try valid query with no spaces
    rawSql =
      RawSqlBuilder
        .parse("select r.id, r.name||'hello' as name from o_customer r ")
        .create();

    query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    list = query.findList();
    Assert.assertNotNull(list);

    rawSql =
      RawSqlBuilder
        .parse("select r.id, r.name||'hello' name from o_customer r ")
        .create();
    query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    list = query.findList();
    Assert.assertNotNull(list);

    // this will barf - expecting the AS keyword now
    rawSql =
      RawSqlBuilder
        .parse("select r.id, r.name || 'hello' name from o_customer r ")
        .create();
    query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    list = query.findList();
    Assert.assertNotNull(list);
  }
}
