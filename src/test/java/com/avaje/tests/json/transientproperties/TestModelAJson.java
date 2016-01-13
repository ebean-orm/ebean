package com.avaje.tests.json.transientproperties;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.PathProperties;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class TestModelAJson {

  @Test
  public void test() {

    ModelA a = new ModelA();
    a.setId(1);
    a.setA("a");

    ModelB b = new ModelB();
    b.setOneField(1);
    b.setTwoField(1);

    a.setList(new ArrayList<ModelB>());
    a.getList().add(b);

    PathProperties pathProperties = PathProperties.parse("(a,list(oneField))");

    String json = Ebean.json().toJson(a, pathProperties);

    assertThat(json).isEqualTo("{\"a\":\"a\",\"list\":[{\"oneField\":1}]}");

  }
}
