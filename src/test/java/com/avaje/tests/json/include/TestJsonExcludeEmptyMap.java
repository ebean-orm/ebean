package com.avaje.tests.json.include;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.JsonConfig;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.tests.model.json.EBasicJsonMap;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

public class TestJsonExcludeEmptyMap {

  @Test
  public void testToBeanToJson_NonNull() throws Exception {

    EBasicJsonMap bean = new EBasicJsonMap();
    bean.setId(99L);
    bean.setContent(new LinkedHashMap<String, Object>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_NULL);

    String asJson = Ebean.json().toJson(bean, options);

    String expectedJson = "{\"id\":99,\"content\":{}}";

    assertEquals(expectedJson, asJson);
  }

  @Test
  public void testToBeanToJson_NonEmpty() throws Exception {

    EBasicJsonMap bean = new EBasicJsonMap();
    bean.setId(99L);
    bean.setContent(new LinkedHashMap<String, Object>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_EMPTY);

    String asJson = Ebean.json().toJson(bean, options);

    String expectedJson = "{\"id\":99,\"content\":{}}";

    assertEquals(expectedJson, asJson);
  }
}
