package org.tests.json.include;

import io.ebean.DB;
import io.ebean.config.JsonConfig;
import io.ebean.text.json.JsonWriteOptions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.AttributeHolder;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonExcludeEmptySet {

  @Test
  public void testToBeanToJson_NonNull() throws Exception {

    AttributeHolder bean = new AttributeHolder();
    bean.setId(99);
    bean.setAttributes(new LinkedHashSet<>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_NULL);

    String asJson = DB.json().toJson(bean, options);

    String expectedJson = "{\"id\":99,\"attributes\":[]}";

    assertEquals(expectedJson, asJson);
  }

  @Test
  public void testToBeanToJson_NonEmpty() throws Exception {

    AttributeHolder bean = new AttributeHolder();
    bean.setId(99);
    bean.setAttributes(new LinkedHashSet<>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_EMPTY);

    String asJson = DB.json().toJson(bean, options);

    String expectedJson = "{\"id\":99}";

    assertEquals(expectedJson, asJson);
  }
}
