package org.tests.idkeys.db;

import javax.persistence.*;

@Entity
public class GenKeySeqB {

  public final static String SEQUENCE_NAME = "GEN_KEY_B_SEQ_NAME";

  private final static String SEQ_GEN_NAME = "SEQ_GEN_NAME";

  /**
   * {@link GeneratedValue#generator()} links to {@link SequenceGenerator#name()}.
   * The name of the sequence is {@link SequenceGenerator#sequenceName()}.
   */
  @Id
  @SequenceGenerator(name = SEQ_GEN_NAME, sequenceName = GenKeySeqB.SEQUENCE_NAME)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_GEN_NAME)
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
