package org.tests.idkeys.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class GenKeySeqA {

  public final static String SEQUENCE_NAME = "GEN_KEY_A_SEQ_NAME";

  /**
   * {@link GeneratedValue#generator()} is not empty, but no {@code SequenceGenerator} is present.
   * So the sequence is named like the generator: {@link #SEQUENCE_NAME}.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
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
