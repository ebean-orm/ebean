package org.tests.json.include;

import io.ebean.Ebean;
import io.ebean.config.JsonConfig;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonWriteOptions;
import org.tests.json.transientproperties.EJsonTransientEntityList;
import org.tests.json.transientproperties.EJsonTransientList;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestJsonExcludeTransientEmptyList {

  @Test
  public void testToBeanToJson_NonNull() throws Exception {

    EJsonTransientList bean = new EJsonTransientList();
    bean.setId(99L);
    bean.setFileNames(new ArrayList<>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_NULL);

    String asJson = Ebean.json().toJson(bean, options);

    String expectedJson = "{\"id\":99,\"fileNames\":[]}";

    assertEquals(expectedJson, asJson);
  }

  @Test
  public void testToBeanToJson_NonEmpty() throws Exception {

    EJsonTransientList bean = new EJsonTransientList();
    bean.setId(99L);
    bean.setFileNames(new ArrayList<>());

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_EMPTY);

    String asJson = Ebean.json().toJson(bean, options);

    String expectedJson = "{\"id\":99}";

    assertEquals(expectedJson, asJson);
  }

  @Test
  public void testToJson_with_transientExcludeFromPathProperties() throws Exception {

    EJsonTransientEntityList bean = new EJsonTransientEntityList();
    bean.setId(99L);
    bean.setName("John");

    PathProperties pathProps = PathProperties.parse("id,name");

    String asJson = Ebean.json().toJson(bean, pathProps);

    assertThat(asJson).isEqualTo("{\"id\":99,\"name\":\"John\"}");
  }
}
