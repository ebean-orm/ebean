package com.avaje.tests.json.include;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.JsonConfig;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.tests.model.basic.Attribute;
import com.avaje.tests.model.basic.AttributeHolder;
import org.junit.Test;

import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;

public class TestJsonExcludeEmptySet {

  @Test
  public void testToBeanToJson_NonNull() throws Exception {

    AttributeHolder bean = new AttributeHolder();
    bean.setId(99);
    bean.setAttributes(new LinkedHashSet<Attribute>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_NULL);

    String asJson = Ebean.json().toJson(bean, options);

    String expectedJson = "{\"id\":99,\"attributes\":[]}";

    assertEquals(expectedJson, asJson);
  }

  @Test
  public void testToBeanToJson_NonEmpty() throws Exception {

    AttributeHolder bean = new AttributeHolder();
    bean.setId(99);
    bean.setAttributes(new LinkedHashSet<Attribute>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_EMPTY);

    String asJson = Ebean.json().toJson(bean, options);

    String expectedJson = "{\"id\":99}";

    assertEquals(expectedJson, asJson);
  }
}
