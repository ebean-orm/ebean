package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionBasic extends BaseTestCase {

  @Test
  public void test() {

    EcPerson person = new EcPerson("Fiona021");
    person.getPhoneNumbers().add("021 1234");
    person.getPhoneNumbers().add("021 4321");
    Ebean.save(person);


    EcPerson person1 = new EcPerson("Fiona09");
    person1.getPhoneNumbers().add("09 1234");
    person1.getPhoneNumbers().add("09 4321");
    Ebean.save(person1);

    List<EcPerson> found =
      Ebean.find(EcPerson.class).where()
        .startsWith("name", "Fiona0")
        .order().asc("id")
      .findList();

    List<String> phoneNumbers0 = found.get(0).getPhoneNumbers();
    List<String> phoneNumbers1 = found.get(1).getPhoneNumbers();
    phoneNumbers0.size();

    assertThat(phoneNumbers0).containsExactly("021 1234", "021 4321");
    assertThat(phoneNumbers1).containsExactly("09 1234", "09 4321");


    List<EcPerson> found2 =
      Ebean.find(EcPerson.class)
        .fetch("phoneNumbers")
        .where()
        .startsWith("name", "Fiona0")
        .order().asc("id")
        .findList();

    System.out.println(found2);
  }
}
