package org.tests.inheritance.abstrakt;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Entity
@Table(name = "block")
@Inheritance
@DiscriminatorColumn(name = "case_type", discriminatorType = DiscriminatorType.INTEGER)
public abstract class AbstractBaseBlock {

  private static int JSON_VERSION = 2;

  @Id
  long id;

  String name;

  @Version
  long version;

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

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  // in version 1 we had not yet the 'case_type'
  public static int migrateRootJson1(ObjectNode node, ObjectMapper mapper) {
    node.put("case_type", "2");
    return 2;
  }

}
