package org.tests.model.interfaces;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTargetEntity extends BaseTestCase {

  @Test
  public void test() {

    Person person = setup();

    Persona persona = new Persona("junk");
    persona.setPerson(person);
    DB.save(persona);

    Persona found = DB.find(Persona.class, persona.getId());
    assertThat(found).isNotNull();
    assertThat(found.persona()).isEqualTo("junk");
    assertThat(found.getPerson().getDefaultAddress().getStreet()).isEqualTo("street");

    DB.delete(persona);
    DB.delete(person);
    DB.delete(person.getDefaultAddress());
  }

  private Person setup() {
    Address address = new Address("street");
    DB.save(address);
    Person person = new Person();
    person.setDefaultAddress(address);
    DB.save(person);
    return person;
  }
}
