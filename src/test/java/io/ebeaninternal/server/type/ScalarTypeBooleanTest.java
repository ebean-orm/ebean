package io.ebeaninternal.server.type;


import io.ebean.Ebean;
import io.ebean.text.json.JsonContext;
import org.tests.model.basic.TOne;
import org.junit.Test;

import static org.junit.Assert.*;

public class ScalarTypeBooleanTest {

  JsonContext jsonContext = Ebean.getDefaultServer().json();

  @Test
  public void json_true() {

    TOne bean = new TOne();
    bean.setId(42);
    bean.setActive(true);

    String json = jsonContext.toJson(bean);
    TOne tOne = jsonContext.toBean(TOne.class, json);

    assertTrue(tOne.isActive());
    assertEquals(json, "{\"id\":42,\"active\":true}");
  }

  @Test
  public void json_false() {

    TOne bean = new TOne();
    bean.setId(42);
    bean.setActive(false);

    String json = jsonContext.toJson(bean);
    TOne tOne = jsonContext.toBean(TOne.class, json);

    assertFalse(tOne.isActive());
    assertEquals(json, "{\"id\":42,\"active\":false}");
  }

}
