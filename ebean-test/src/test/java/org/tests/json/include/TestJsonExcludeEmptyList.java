package org.tests.json.include;

import io.ebean.DB;
import io.ebean.config.JsonConfig;
import io.ebean.text.json.JsonWriteOptions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonExcludeEmptyList {

  @Test
  public void testToBeanToJson_NonNull() throws Exception {

    Order bean = new Order();
    bean.setId(99);
    bean.setStatus(null);
    bean.setOrderDate(null);
    bean.setDetails(new ArrayList<>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_NULL);

    String asJson = DB.json().toJson(bean, options);

    String expectedJson = "{\"id\":99,\"details\":[]}";

    assertEquals(expectedJson, asJson);
  }

  @Test
  public void testToBeanToJson_NonEmpty() throws Exception {

    Order bean = new Order();
    bean.setId(99);
    bean.setStatus(null);
    bean.setOrderDate(null);
    bean.setDetails(new ArrayList<>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_EMPTY);

    String asJson = DB.json().toJson(bean, options);

    String expectedJson = "{\"id\":99}";

    assertEquals(expectedJson, asJson);
  }
}
