package org.tests.json.include;

import io.ebean.Ebean;
import io.ebean.config.JsonConfig;
import io.ebean.text.json.JsonWriteOptions;
import org.tests.model.json.EBasicJsonMap;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

public class TestJsonExcludeEmptyMap {

  @Test
  public void testToBeanToJson_NonNull() throws Exception {

    EBasicJsonMap bean = new EBasicJsonMap();
    bean.setId(99L);
    bean.setContent(new LinkedHashMap<>());

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
    bean.setContent(new LinkedHashMap<>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_EMPTY);

    String asJson = Ebean.json().toJson(bean, options);

    String expectedJson = "{\"id\":99,\"content\":{}}";

    assertEquals(expectedJson, asJson);
  }
}
