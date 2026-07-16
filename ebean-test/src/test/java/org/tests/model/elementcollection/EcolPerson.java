package org.tests.model.elementcollection;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.List;

/**
 * Element collection of embeddable values with an explicit {@code @OrderColumn}.
 */
@Entity
public class EcolPerson {

  @Id
  long id;

  String name;

  @ElementCollection
  @CollectionTable(joinColumns = @JoinColumn(name = "person_id"))
  @OrderColumn(name = "ordinal")
  List<EcPhone> phoneNumbers = new ArrayList<>();

  @Version
  long version;

  public EcolPerson(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "person id:" + id + " name:" + name + " phs:" + phoneNumbers;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<EcPhone> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(List<EcPhone> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
