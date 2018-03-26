package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import java.util.List;
import java.util.Set;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionBasicSet extends BaseTestCase {

  @Test
  public void test() {

    EcsPerson person = new EcsPerson("Fiona021");
    person.getPhoneNumbers().add("021 1234");
    person.getPhoneNumbers().add("021 4321");
    Ebean.save(person);


    EcsPerson person1 = new EcsPerson("Fiona09");
    person1.getPhoneNumbers().add("09 1234");
    person1.getPhoneNumbers().add("09 4321");
    person1.getPhoneNumbers().add("09 9876");
    Ebean.save(person1);

    List<EcsPerson> found =
      Ebean.find(EcsPerson.class).where()
        .startsWith("name", "Fiona0")
        .order().asc("id")
        .findList();

    Set<String> phoneNumbers0 = found.get(0).getPhoneNumbers();
    Set<String> phoneNumbers1 = found.get(1).getPhoneNumbers();
    phoneNumbers0.size();

    assertThat(phoneNumbers0).containsExactly("021 1234", "021 4321");
    assertThat(phoneNumbers1).containsExactly("09 1234", "09 4321", "09 9876");


    List<EcsPerson> found2 =
      Ebean.find(EcsPerson.class)
        .fetch("phoneNumbers")
        .where()
        .startsWith("name", "Fiona0")
        .order().asc("id")
        .findList();

    assertThat(found2).hasSize(2);
    EcsPerson foundFirst = found2.get(0);
    String asJson = Ebean.json().toJson(foundFirst);

    EcsPerson fromJson = Ebean.json().toBean(EcsPerson.class, asJson);

    assertThat(fromJson.getPhoneNumbers()).containsAll(foundFirst.getPhoneNumbers());
  }
}
