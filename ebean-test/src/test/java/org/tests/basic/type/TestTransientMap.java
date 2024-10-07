package org.tests.basic.type;

import io.ebean.Query;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TestTransientMap extends BaseTestCase {

  @Test
  void testMe() {

    Map<String, List<String>> map = new HashMap<>();
    map.put("foo", new ArrayList<>());

    BSimpleWithGen b = new BSimpleWithGen("blah");
    b.setSomeMap(map);
    b.setDescription("hi");
    DB.save(b);

    final BSimpleWithGen found = DB.find(BSimpleWithGen.class, b.getId());
    assertThat(found.getName()).isEqualTo("blah");
    assertThat(found.getSomeMap()).isNull();

    Query<BSimpleWithGen> query = DB.find(BSimpleWithGen.class)
      .where().startsWith("description", "h")
      .orderBy().desc("description");

    var list = query.findList();
    assertThat(list).hasSize(1);
    assertThat(query.getGeneratedSql()).contains("where t0.description like");
    assertThat(query.getGeneratedSql()).contains("order by t0.description desc");

    DB.delete(b);
  }
}
