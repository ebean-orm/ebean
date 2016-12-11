package org.tests.idkeys.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
@SequenceGenerator(name = "SEQ_NAME", sequenceName = GenKeySequence.SEQUENCE_NAME)
public class GenKeySequence {
  public final static String SEQUENCE_NAME = "SEQ";

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_NAME")
  private Long id;

  private String description;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
