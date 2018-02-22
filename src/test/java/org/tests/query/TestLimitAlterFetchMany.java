package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

public class TestLimitAlterFetchMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    // Due to the use of maxRows... we will convert
    // the fetch join to contacts over to a query join
    // ... otherwise we wouldn't be able to use the
    // limit offset clause

    Query<Customer> query = Ebean.find(Customer.class)
      // this will automatically get converted to a
      // query join ... due to the maxRows
      .fetch("contacts").setMaxRows(5).orderBy("id");

    List<Customer> list = query.findList();

    System.out.println(list);

  }

}
