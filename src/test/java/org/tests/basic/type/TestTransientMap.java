package org.tests.basic.type;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTransientMap extends BaseTestCase {

  @Test
  public void testMe() {

    Map<String, List<String>> map = new HashMap<>();
    map.put("foo", new ArrayList<>());

    BSimpleWithGen b = new BSimpleWithGen("blah");
    b.setSomeMap(map);
    DB.save(b);

    final BSimpleWithGen found = DB.find(BSimpleWithGen.class, b.getId());
    assertThat(found.getName()).isEqualTo("blah");
    assertThat(found.getSomeMap()).isNull();

    DB.delete(b);
  }
}
