package com.avaje.tests.text.json;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

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
