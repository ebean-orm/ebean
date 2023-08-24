package org.tests.model.basic;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity(name = "Person")
@Table(name = "PERSONS")
public class Person implements Serializable {

  private static final long serialVersionUID = 495045977245770183L;

  @Id
  @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
  @SequenceGenerator(name = "PERSONS_SEQ", initialValue = 1000, allocationSize = 40)
  @Column(name = "id", unique = true, nullable = false)
  private Long id;

  @Column(name = "SURNAME", nullable = false, unique = false, columnDefinition = "varchar(64)")
  private String surname;

  @Column(name = "NAME", nullable = false, unique = false, columnDefinition = "varchar(64)")
  private String name;

  @OneToMany(targetEntity = Phone.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "person")
  private List<Phone> phones;

  public Person() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Phone> getPhones() {
    return phones;
  }

  public void setPhones(List<Phone> phones) {
    this.phones = phones;
  }

}
