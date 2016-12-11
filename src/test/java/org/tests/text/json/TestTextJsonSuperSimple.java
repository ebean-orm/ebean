package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.text.json.JsonContext;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TestTextJsonSuperSimple extends BaseTestCase {

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).select("id, name").order().desc("id")
      .findList();

    EbeanServer server = Ebean.getServer(null);

    JsonContext json = server.json();

    if (list.size() > 1) {
      Customer customer = list.get(0);

      String s = json.toJson(customer);
      int statusPos = s.indexOf("status");
      Assert.assertEquals(-1, statusPos);
    }

  }
}
