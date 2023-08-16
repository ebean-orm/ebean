package org.tests.model.basic.cache;

import io.ebean.DB;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestCustomExtends {

  @Test
  void insert_find() {
    OCachedApp orig = new OCachedApp("orig");
    DB.save(orig);

    OCachedAppCustom custom = new OCachedAppCustom("custom");
    custom.setCustom("someCustomValue");
    DB.save(custom);

    OCachedAppCustom c1 = DB.find(OCachedAppCustom.class, custom.getId());
    OCachedApp c2 = DB.find(OCachedApp.class, custom.getId());

    OCachedAppCustom o1 = DB.find(OCachedAppCustom.class, orig.getId());
    OCachedApp o2 = DB.find(OCachedApp.class, orig.getId());

    assertThat(c1).isNotNull();
    assertThat(c2).isNotNull();
    assertThat(c2.getAppName()).isEqualTo(c1.getAppName());
    assertThat(c2).isNotNull().isInstanceOf(OCachedAppCustom.class);

    assertThat(o1).isNotNull();
    assertThat(o2).isNotNull();
    assertThat(o2.getAppName()).isEqualTo(o1.getAppName());
    assertThat(o2).isNotNull().isInstanceOf(OCachedAppCustom.class);
  }
}
