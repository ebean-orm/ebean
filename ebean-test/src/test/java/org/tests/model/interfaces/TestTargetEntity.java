package org.tests.model.interfaces;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestTargetEntity extends BaseTestCase {

  @Test
  void test() {
    Person person = setup();

    Persona persona = new Persona("junk");
    persona.setPerson(person);
    DB.save(persona);

    Persona found = DB.find(Persona.class, persona.getId());
    assertThat(found).isNotNull();
    assertThat(found.persona()).isEqualTo("junk");
    IPerson person1 = found.getPerson();
    IAddress address = person1.getDefaultAddress();
    assertThat(address.getStreet()).isEqualTo("street");

    person1.setDefaultAddress(null);
    DB.save(person1);
    DB.delete(address);
    DB.delete(persona);
    DB.delete(person1);
  }

  @Test
  void addresses() {
    Person p0 = new Person();
    Person p1 = new Person();
    DB.saveAll(p0, p1);

    Address address0 = new Address("a0", p0);
    Address address1 = new Address("a1", p0);
    Address address2 = new Address("a2", p1);
    Address address3 = new Address("a3", p1);
    DB.saveAll(address0, address1, address2, address3);

    p0.setDefaultAddress(address0);
    p1.setDefaultAddress(address2);
    DB.saveAll(p0, p1);

    List<Person> list = DB.find(Person.class).findList();
    for (Person person : list) {
      IAddress da0 = person.getDefaultAddress();
      da0.getStreet();
    }

    list.get(0).getAddresses()
      .size();
  }

  private Person setup() {
    Person person = new Person();
    DB.save(person);
    Address address = new Address("street", person);
    DB.save(address);
    person.setDefaultAddress(address);
    DB.save(person);
    return person;
  }
}
