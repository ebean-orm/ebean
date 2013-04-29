package com.avaje.tests.query;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryInIdTypeConversion extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<String> idList = new ArrayList<String>();
    idList.add("1");
    idList.add("2");

    List<Customer> list = Ebean.find(Customer.class).where().idIn(idList).findList();

    Assert.assertNotNull(list);

  }
}
