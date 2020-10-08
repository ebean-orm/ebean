package org.tests.model.elementcollection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
public class EcTop {

  @Id
  private long id;

  @Version
  private long version;

  private final String name;

  @ManyToOne(cascade = ALL)
  private EcsPerson person;

  /**
   * Not normally expect cascade on ManyToMany but here for this test.
   */
  @ManyToMany(cascade = ALL)
  private List<EcsPerson> people;

  public EcTop(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public EcsPerson getPerson() {
    return person;
  }

  public void setPerson(EcsPerson person) {
    this.person = person;
  }

  public List<EcsPerson> getPeople() {
    return people;
  }

  public void setPeople(List<EcsPerson> people) {
    this.people = people;
  }
}
