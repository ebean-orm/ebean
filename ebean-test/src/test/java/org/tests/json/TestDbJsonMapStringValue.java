package org.tests.json;

import io.ebean.DB;
import io.ebean.annotation.DbJsonB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression: @DbJsonB Map&lt;String,String&gt; (typed value map, handled via the Jackson
 * ObjectMapper path) alongside Map&lt;String,Object&gt; (handled by the built-in type).
 */
class TestDbJsonMapStringValue extends BaseTestCase {

  @Entity
  @Table(name = "json_map_string_value")
  public static class MapStringBean {
    @Id
    Long id;

    @DbJsonB
    Map<String, String> stringMap;

    @DbJsonB
    Map<String, Object> objectMap;

    public MapStringBean(Long id) {
      this.id = id;
    }

    public MapStringBean() {
    }
  }

  @Test
  void mapStringString_roundTrip() {
    MapStringBean bean = new MapStringBean(1L);
    Map<String, String> m = new LinkedHashMap<>();
    m.put("a", "alpha");
    m.put("b", "beta");
    bean.stringMap = m;

    Map<String, Object> om = new LinkedHashMap<>();
    om.put("x", "ex");
    om.put("n", 42L);
    bean.objectMap = om;

    DB.save(bean);

    MapStringBean found = DB.find(MapStringBean.class, 1L);
    assertThat(found.stringMap).containsEntry("a", "alpha").containsEntry("b", "beta");
    assertThat(found.objectMap).containsEntry("x", "ex").containsEntry("n", 42L);

    found.stringMap.put("c", "gamma");
    DB.save(found);
    assertThat(DB.find(MapStringBean.class, 1L).stringMap).hasSize(3).containsEntry("c", "gamma");
  }
}
