package org.tests.text.json;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonWriteOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.io.IOException;
import java.util.List;

public class TestTextJsonInvokeLazy extends BaseTestCase {

  @BeforeEach
  public void clearBeanCache() {
    server().pluginApi().beanType(Customer.class).clearBeanCache();
  }

  @Test
  public void test() throws IOException {
    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class).select("name").findList();

    JsonWriteOptions opt = JsonWriteOptions.parsePath("name, status");

    JsonContext jsonContext = DB.json();
    jsonContext.toJson(list, opt);
  }
}
