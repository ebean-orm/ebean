package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.text.json.JsonContext;
import io.ebean.util.StringHelper;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.io.IOException;

public class TestTextJsonInsertUpdate extends BaseTestCase {

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    String json0 = "{\"name\":\"InsJson\",\"status\":\"NEW\"}";

    JsonContext jsonContext = Ebean.json();

    // insert
    Customer c0 = jsonContext.toBean(Customer.class, json0);
    Ebean.save(c0);

    // update with optimistic concurrency checking
    String j0 = jsonContext.toJson(c0);
    String j1 = StringHelper.replaceString(j0, "InsJson", "Mod1");
    Customer c1 = jsonContext.toBean(Customer.class, j1);
    Ebean.update(c1);

    // update with no optimistic concurrency checking
    String j2 = "{\"id\":" + c0.getId() + ",\"name\":\"ModIns\",\"status\":\"ACTIVE\"}";
    Customer c2 = jsonContext.toBean(Customer.class, j2);
    Ebean.update(c2);

  }
}
