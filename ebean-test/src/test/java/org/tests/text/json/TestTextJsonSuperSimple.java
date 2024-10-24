package org.tests.text.json;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.text.json.JsonContext;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTextJsonSuperSimple extends BaseTestCase {

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class).select("id, name").orderBy().desc("id")
      .findList();

    Database server = DB.getDefault();

    JsonContext json = server.json();

    if (list.size() > 1) {
      Customer customer = list.get(0);

      String s = json.toJson(customer);
      int statusPos = s.indexOf("status");
      assertEquals(-1, statusPos);
    }

  }
}
