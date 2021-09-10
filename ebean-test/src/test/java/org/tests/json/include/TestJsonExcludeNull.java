package org.tests.json.include;

import io.ebean.DB;
import io.ebean.config.JsonConfig;
import io.ebean.text.json.JsonWriteOptions;
import org.junit.jupiter.api.Test;
import org.tests.json.transientproperties.EJsonTransientObject;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonExcludeNull {

  @Test
  public void testToBeanToJson() throws Exception {


    EJsonTransientObject bean = new EJsonTransientObject();
    bean.setId(99L);
    bean.setName(null);

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_NULL);

    String asJson = DB.json().toJson(bean, options);

    String expectedJson = "{\"id\":99}";

    assertEquals(expectedJson, asJson);
  }

}
