package org.tests.model.interfaces;

import javax.persistence.*;

@Entity
public class Persona implements IPersona {

  @Id
  private long id;

  @Version
  private int version;

  private final String persona;

  @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY, targetEntity = Person.class)
  private IPerson person;

  public Persona(String persona) {
    this.persona = persona;
  }

  public long getId() {
    return id;
  }

  @Override
  public String persona() {
    return persona;
  }

  public void setPerson(IPerson person) {
    this.person = person;
  }

  public IPerson getPerson() {
    return person;
  }
}
