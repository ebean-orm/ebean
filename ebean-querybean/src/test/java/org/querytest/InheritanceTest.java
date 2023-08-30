package org.querytest;

import org.example.domain.ACat;
import org.example.domain.query.QACat;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InheritanceTest {

  @Test
  void query_setInheritType() {

    ACat cat = new ACat("C1");
    cat.save();

    ACat cat2 = new ACat("C2");
    cat2.save();

    List<ACat> animals = new QACat()
      .id.greaterOrEqualTo(1L)
      .findList();

    assertThat(animals).isNotEmpty();
  }

  @Test
  void query_onInheritedProperty() {
    new QACat()
      .name.eq("foo")
      .catProp.eq("foo")
      .findList();
  }

}
