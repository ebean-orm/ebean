package org.tests.cascade;

import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestJsonMarshallingOTM {

  @Test
  public void test() {

    String json = ("" +
      "{ 'masterRel': [" +
      "  { 'name': 'a' }," +
      "  { 'name': 'b' }" +
      "] }"
    ).replaceAll("'", "\"");

    RelDetail bean = DB.json().toBean(RelDetail.class, json);

    final RelMaster m0 = bean.getMasterRel().get(0);
    assertThat(m0.getName()).isEqualTo("a");
    final RelMaster m1 = bean.getMasterRel().get(1);
    assertThat(m1.getName()).isEqualTo("b");
  }
}
