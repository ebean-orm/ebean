package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonWriteOptions;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TestTextJsonInvokeLazy extends BaseTestCase {

  @Before
  public void clearBeanCache() {
    server().getPluginApi().getBeanType(Customer.class).clearBeanCache();
  }
  
  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).select("name").findList();

    JsonWriteOptions opt = JsonWriteOptions.parsePath("name, status");

    JsonContext jsonContext = Ebean.json();
    jsonContext.toJson(list, opt);
  }
}
