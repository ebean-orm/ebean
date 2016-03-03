package com.avaje.ebeaninternal.server.type;


import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.TOne;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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