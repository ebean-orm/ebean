package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonWriteOptions;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TestTextJsonReadManyLazyLoad extends BaseTestCase {

  @Test
  public void test_lazyLoadBoth() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).select("id, status, shippingAddress").order()
      .desc("id").findList();

    JsonContext json = Ebean.json();

    // test that lazy loading on
    PathProperties pp = PathProperties.parse("(id,name,contacts(firstName))");
    JsonWriteOptions o = new JsonWriteOptions();
    o.setPathProperties(pp);

    String s = json.toJson(list, o);
    Assert.assertTrue(s.contains("\"contacts\""));
    Assert.assertTrue(s.contains("\"name\""));

  }

  @Test
  public void test_lazyLoadCust() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).select("id, status, shippingAddress")
      .fetch("contacts").order().desc("id").findList();

    JsonContext json = Ebean.json();

    // test that lazy loading on
    PathProperties pp = PathProperties.parse("(id,name,contacts(firstName))");
    JsonWriteOptions o = new JsonWriteOptions();
    o.setPathProperties(pp);

    String s = json.toJson(list, o);
    Assert.assertTrue(s.contains("\"contacts\""));
    Assert.assertTrue(s.contains("\"name\""));

  }

  @Test
  public void test_lazyLoadContacts() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).select("id, name, status, shippingAddress")
      // .fetch("contacts")
      .order().desc("id").findList();

    JsonContext json = Ebean.json();

    // test that lazy loading on
    PathProperties pp = PathProperties.parse("(id,name,contacts(firstName))");
    JsonWriteOptions o = new JsonWriteOptions();
    o.setPathProperties(pp);

    String s = json.toJson(list, o);
    Assert.assertTrue(s.contains("\"contacts\""));
    Assert.assertTrue(s.contains("\"name\""));

  }

}
