package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

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
      .fetch("contacts").setMaxRows(5);

    List<Customer> list = query.findList();

    System.out.println(list);

  }

}
