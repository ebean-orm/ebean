package com.avaje.tests.json.include;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.JsonConfig;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.tests.json.transientproperties.EJsonTransientObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestJsonExcludeNull {

  @Test
  public void testToBeanToJson() throws Exception {


    EJsonTransientObject bean = new EJsonTransientObject();
    bean.setId(99L);
    bean.setName(null);

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_NULL);

    String asJson = Ebean.json().toJson(bean, options);

    String expectedJson = "{\"id\":99}";

    assertEquals(expectedJson, asJson);
  }

}
