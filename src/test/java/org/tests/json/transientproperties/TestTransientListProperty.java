package org.tests.json.transientproperties;

import io.ebean.Ebean;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTransientListProperty {

  @Test
  public void testWithTransientListProperty() throws Exception {

    String rawJson = "{\"basic\":true,\"fileNames\":[\"1\"]}";

    EJsonTransientList bean = Ebean.json().toBean(EJsonTransientList.class, rawJson);

    assertEquals(true, bean.getBasic());
    assertEquals(1, bean.getFileNames().size());

    String asJson = Ebean.json().toJson(bean);

    assertEquals(rawJson, asJson);
  }

}
