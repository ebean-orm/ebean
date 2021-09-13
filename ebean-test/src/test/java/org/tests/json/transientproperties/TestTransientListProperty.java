package org.tests.json.transientproperties;

import io.ebean.DB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTransientListProperty {

  @Test
  public void testWithTransientListProperty() throws Exception {

    String rawJson = "{\"basic\":true,\"fileNames\":[\"1\"]}";

    EJsonTransientList bean = DB.json().toBean(EJsonTransientList.class, rawJson);

    assertEquals(true, bean.getBasic());
    assertEquals(1, bean.getFileNames().size());

    String asJson = DB.json().toJson(bean);

    assertEquals(rawJson, asJson);
  }

}
