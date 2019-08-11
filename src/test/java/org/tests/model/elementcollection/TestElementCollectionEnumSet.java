package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionEnumSet extends BaseTestCase {

  @Test
  public void test() {

    EcEnumPerson person = new EcEnumPerson("Enum Person");
    person.getTags().add(EcEnumPerson.Tags.BLUE);
    person.getTags().add(EcEnumPerson.Tags.RED);

    Ebean.save(person);


    EcEnumPerson one = Ebean.find(EcEnumPerson.class)
      .setId(person.getId())
      .fetch("tags")
      .findOne();

    assertThat(one.getTags()).hasSize(2);

    one.getTags().add(EcEnumPerson.Tags.GREEN);
    one.getTags().remove(EcEnumPerson.Tags.BLUE);

    Ebean.save(one);
  }
}
