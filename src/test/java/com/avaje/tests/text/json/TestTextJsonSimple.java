package com.avaje.tests.text.json;

import java.util.List;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonWriteBeanVisitor;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.ebean.text.json.JsonWriter;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestTextJsonSimple extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).select("id, name, status, shippingAddress")
        .fetch("billingAddress", "line1, city").fetch("billingAddress.country", "*")
        .fetch("contacts", "firstName,email")// , new
                                             // FetchConfig().queryFirst(2))
        // .filterMany("contacts").ilike("firstName", "J%").query()
        // .where().lt("id", 3)
        .order().desc("id").findList();

    EbeanServer server = Ebean.getServer(null);

    JsonContext json = server.createJsonContext();

    JsonWriteOptions options = new JsonWriteOptions();
    options.setRootPathProperties("name, id");
    options.setRootPathVisitor(new JsonWriteBeanVisitor<Customer>() {

      public void visit(Customer bean, JsonWriter ctx) {
        System.out.println("visiting " + bean);
        ctx.appendRawValue("dummy", "34");
      }

    });

     String s = json.toJsonString(list, true);
//    String s = json.toJsonString(list, true, options);

    System.out.println(s);

    List<Customer> mList = json.toList(Customer.class, s);
    System.out.println(mList);

  }
}
