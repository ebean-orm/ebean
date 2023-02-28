package org.querytest;

import org.example.domain.ACat;
import org.example.domain.ADog;
import org.example.domain.Animal;
import org.example.domain.query.QACat;
import org.example.domain.query.QASuperCat;
import org.example.domain.query.QAnimal;
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

    ADog dog = new ADog("D1", "D878");
    dog.save();

    List<Animal> animals = new QAnimal()
      .id.greaterOrEqualTo(1L)
      .setInheritType(ACat.class)
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

  @Test
  void query_onInheritedProperty_whenParentOnlyHasDiscriminator() {
    // the .name and .catProp properties exist on QASuperCat()
    new QASuperCat()
      .superCat.eq("foo")
      .name.eq("foo")
      .catProp.eq("foo")
      .findList();
  }
}
