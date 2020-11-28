package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionEnumSet extends BaseTestCase {

  @Test
  public void test() {

    EcEnumPerson person = new EcEnumPerson("Enum Person");
    person.getTags().add(EcEnumPerson.Tags.BLUE);
    person.getTags().add(EcEnumPerson.Tags.RED);

    DB.save(person);


    EcEnumPerson one = DB.find(EcEnumPerson.class)
      .setId(person.getId())
      .fetch("tags")
      .findOne();

    assertThat(one.getTags()).hasSize(2);

    one.getTags().add(EcEnumPerson.Tags.GREEN);
    one.getTags().remove(EcEnumPerson.Tags.BLUE);

    DB.save(one);
  }

  @Test
  public void json() {

    EcEnumPerson person = new EcEnumPerson("Enum Person");
    person.getTags().add(EcEnumPerson.Tags.BLUE);
    person.getTags().add(EcEnumPerson.Tags.RED);

    final String asJson = DB.json().toJson(person);

    assertThat(asJson).isEqualTo("{\"name\":\"Enum Person\",\"tags\":[\"BLUE\",\"RED\"]}");

    final EcEnumPerson fromJson = DB.json().toBean(EcEnumPerson.class, asJson);
    assertThat(fromJson.getName()).isEqualTo("Enum Person");
    assertThat(fromJson.getTags()).hasSize(2);
    assertThat(fromJson.getTags().toString()).isEqualTo("BeanSet size[2] set[BLUE, RED]");
  }
}
