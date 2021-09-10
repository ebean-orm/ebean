package org.tests.json;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EBasicJsonUnmapped;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUnmappedProperties extends BaseTestCase {

  @Test
  public void toJson() {

    Map<String,Object> nested = new LinkedHashMap<>();
    nested.put("alpha", "aa");
    nested.put("beta", "bb");


    Map<String,Object> unmapped = new LinkedHashMap<>();
    unmapped.put("one", 42);
    unmapped.put("nested", nested);

    EBasicJsonUnmapped bean = new EBasicJsonUnmapped();
    bean.setName("someName");
    bean.setUnmapped(unmapped);


    String asJson = DB.json().toJson(bean);

    assertThat(asJson).contains("{\"name\":\"someName\",\"one\":42,\"nested\":{\"alpha\":\"aa\",\"beta\":\"bb\"}}");

  }

  @Test
  public void fromJson() {

    String json = "{\"name\":\"someName\",\"one\":42,\"nested\":{\"alpha\":\"aa\",\"beta\":\"bb\"}}";

    EBasicJsonUnmapped bean = DB.json().toBean(EBasicJsonUnmapped.class, json);

    assertThat(bean.getName()).isEqualTo("someName");
    assertThat(bean.getUnmapped().get("one")).isEqualTo(42L);

    @SuppressWarnings("unchecked")
    Map<String,Object> fromNested = (Map<String,Object>)bean.getUnmapped().get("nested");
    assertThat(fromNested).containsKeys("alpha", "beta");
    assertThat(fromNested).containsValues("aa", "bb");
  }

}
