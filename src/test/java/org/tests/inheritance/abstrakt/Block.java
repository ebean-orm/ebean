package org.tests.inheritance.abstrakt;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Entity
@DiscriminatorValue(value = "2")
public class Block extends AbstractBaseBlock {

  public static final int JSON_VERSION = 3; // to test Json-Migration

  String notes;

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  // version 2->3 transfer the 'xxx' to the 'name' property
  public static int migrateJson2(ObjectNode node, ObjectMapper mapper) {
    node.put("name", node.remove("xxx").asText());
    return 3;
  }
}
