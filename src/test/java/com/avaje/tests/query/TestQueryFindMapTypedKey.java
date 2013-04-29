package com.avaje.tests.query;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryFindMapTypedKey extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Map<String, Customer> map = Ebean.find(Customer.class).select("id, name")
        .findMap("name", String.class);

    Assert.assertNotNull(map);
  }
}
