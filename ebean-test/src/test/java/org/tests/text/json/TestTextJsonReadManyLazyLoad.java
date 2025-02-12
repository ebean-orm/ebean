package org.tests.text.json;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonWriteOptions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTextJsonReadManyLazyLoad extends BaseTestCase {

  @Test
  public void test_lazyLoadBoth() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class).select("id, status, shippingAddress").orderBy()
      .desc("id").findList();

    JsonContext json = DB.json();

    // test that lazy loading on
    PathProperties pp = PathProperties.parse("(id,name,contacts(firstName))");
    JsonWriteOptions o = new JsonWriteOptions();
    o.setPathProperties(pp);

    String s = json.toJson(list, o);
    assertTrue(s.contains("\"contacts\""));
    assertTrue(s.contains("\"name\""));
  }

  @Test
  public void test_lazyLoadCust() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class).select("id, status, shippingAddress")
      .fetch("contacts").orderBy().desc("id").findList();

    JsonContext json = DB.json();

    // test that lazy loading on
    PathProperties pp = PathProperties.parse("(id,name,contacts(firstName))");
    JsonWriteOptions o = new JsonWriteOptions();
    o.setPathProperties(pp);

    String s = json.toJson(list, o);
    assertTrue(s.contains("\"contacts\""));
    assertTrue(s.contains("\"name\""));
  }

  @Test
  public void test_lazyLoadContacts() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class).select("id, name, status, shippingAddress")
      // .fetch("contacts")
      .orderBy().desc("id").findList();

    JsonContext json = DB.json();

    // test that lazy loading on
    PathProperties pp = PathProperties.parse("(id,name,contacts(firstName))");
    JsonWriteOptions o = new JsonWriteOptions();
    o.setPathProperties(pp);

    String s = json.toJson(list, o);
    assertTrue(s.contains("\"contacts\""));
    assertTrue(s.contains("\"name\""));
  }

}
