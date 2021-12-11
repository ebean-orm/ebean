package org.tests.o2m;

import io.ebean.BaseTestCase;
import io.ebean.common.BeanList;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EBasicJsonMap;
import org.tests.o2m.lazy.OmlBaz;
import org.tests.o2m.lazy.OmlFoo;

import static org.assertj.core.api.Assertions.assertThat;

class TestOneToManyEnhancement extends BaseTestCase {

  @Test
  void test_when_constructorAddsEntry() {
    OmlFoo foo = new OmlFoo(new OmlBaz());

    assertThat(foo.getBazList()).isInstanceOf(BeanList.class);
    assertThat(foo.getBazList()).isNotEmpty();
  }

  @Test
  void test_commonCase() {
    EBasicJsonMap bean = new EBasicJsonMap();

    assertThat(bean.getDetails()).isInstanceOf(BeanList.class);
    assertThat(bean.getDetails()).isEmpty();
  }
}
