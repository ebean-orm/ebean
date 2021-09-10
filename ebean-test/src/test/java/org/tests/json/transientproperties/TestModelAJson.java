package org.tests.json.transientproperties;

import io.ebean.DB;
import io.ebean.text.PathProperties;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class TestModelAJson {

  @Test
  public void test() {

    ModelA a = new ModelA();
    a.setId(1);
    a.setA("a");

    ModelB b = new ModelB();
    b.setOneField(1);
    b.setTwoField(1);

    a.setList(new ArrayList<>());
    a.getList().add(b);

    PathProperties pathProperties = PathProperties.parse("(a,list(oneField))");

    String json = DB.json().toJson(a, pathProperties);

    assertThat(json).isEqualTo("{\"a\":\"a\",\"list\":[{\"oneField\":1}]}");

  }
}
