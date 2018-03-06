package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.config.JsonConfig.Include;
import io.ebean.text.json.JsonWriteOptions;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

public class TestJsonWriteOptions extends BaseTestCase {

  @Before
  public void prepare() {
    ResetBasicData.reset();
    Ebean.getServerCacheManager().getBeanCache(Order.class).clear();
  }

  @Test
  public void testNoOpts() throws IOException {

    Order o = Ebean.find(Order.class).setId(1).fetch("shipments").findOne();

    JsonWriteOptions jsonOpts = new JsonWriteOptions();
    String s = Ebean.json().toJson(o, jsonOpts);

    s = s.replaceAll("\\d{5,}", "XXXX"); // replaces all timestamps
    assertThat(s).isEqualTo("{\"id\":1,\"status\":\"NEW\",\"orderDate\":XXXX,\"shipDate\":null,"
        + "\"customer\":{\"id\":1},\"customerName\":\"Rob\","
        + "\"shipments\":[{\"id\":1,\"shipTime\":XXXX,\"cretime\":XXXX,\"updtime\":XXXX,\"version\":1}],"
        + "\"cretime\":XXXX,\"updtime\":XXXX,\"totalAmount\":null,\"totalItems\":null}");

  }

  @Test
  public void testIncludeNonEmpty() throws IOException {

    Order o = Ebean.find(Order.class).setId(1).fetch("shipments").findOne();

    JsonWriteOptions jsonOpts = new JsonWriteOptions();
    jsonOpts.setInclude(Include.NON_EMPTY);
    String s = Ebean.json().toJson(o, jsonOpts);

    s = s.replaceAll("\\d{5,}", "XXXX");
    assertThat(s).isEqualTo("{\"id\":1,\"status\":\"NEW\",\"orderDate\":XXXX,"
        + "\"customer\":{\"id\":1},\"customerName\":\"Rob\","
        + "\"shipments\":[{\"id\":1,\"shipTime\":XXXX,\"cretime\":XXXX,\"updtime\":XXXX,\"version\":1}],"
        + "\"cretime\":XXXX,\"updtime\":XXXX}");

  }

  @Test
  public void testIncludeForceReference() throws IOException {

    Order o = Ebean.find(Order.class).setId(1).fetch("shipments").findOne();

    JsonWriteOptions jsonOpts = new JsonWriteOptions();
    jsonOpts.setForceReference(true);
    String s = Ebean.json().toJson(o, jsonOpts);

    s = s.replaceAll("\\d{5,}", "XXXX");
    assertThat(s).isEqualTo("{\"id\":1,\"status\":\"NEW\",\"orderDate\":XXXX,\"shipDate\":null,"
        + "\"customer\":{\"id\":1},\"customerName\":\"Rob\","
        + "\"shipments\":[{\"id\":1}],"
        + "\"cretime\":XXXX,\"updtime\":XXXX,\"totalAmount\":null,\"totalItems\":null}");
  }

  @Test
  public void testIncludeForceReferenceNonEmpty() throws IOException {

    Order o = Ebean.find(Order.class).setId(1).fetch("shipments").findOne();

    JsonWriteOptions jsonOpts = new JsonWriteOptions();
    jsonOpts.setForceReference(true);
    jsonOpts.setInclude(Include.NON_EMPTY);
    String s = Ebean.json().toJson(o, jsonOpts);

    s = s.replaceAll("\\d{5,}", "XXXX");
    assertThat(s).isEqualTo("{\"id\":1,\"status\":\"NEW\",\"orderDate\":XXXX,"
        + "\"customer\":{\"id\":1},\"customerName\":\"Rob\","
        + "\"shipments\":[{\"id\":1}],"
        + "\"cretime\":XXXX,\"updtime\":XXXX}");
  }
}
