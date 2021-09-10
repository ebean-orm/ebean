package io.ebeaninternal.server.type;


import io.ebean.DB;
import io.ebean.text.json.JsonContext;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TOne;

import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ScalarTypeBooleanTest {

  JsonContext jsonContext = DB.getDefault().json();

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


  @Test
  public void IntBoolean_isLogicalBoolean() {

    ScalarTypeBoolean.IntBoolean intBoolean = new ScalarTypeBoolean.IntBoolean(1, 0);
    assertThat(intBoolean).isInstanceOf(ScalarTypeLogicalType.class);

    assertThat(intBoolean.getLogicalType()).isEqualTo(Types.BOOLEAN);
  }
}
