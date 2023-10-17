package org.tests.model.composite;


import io.ebean.annotation.Index;
import jakarta.persistence.*;

import java.util.UUID;

@IdClass(DataWithFormulaKey.class)
@Entity
public class Data2WithFormula {

  @Id
  private UUID mainId;

  @Id
  private String metaKey;

  @Id
  private Integer valueIndex;

  @ManyToOne
  @JoinColumn(name = "main_id", insertable = false, nullable = false)
  private Data2WithFormulaMain main;

  @Index
  private String stringValue;

  public UUID mainId() {
    return mainId;
  }

  public void setMainId(UUID mainId) {
    this.mainId = mainId;
  }

  public String getMetaKey() {
    return metaKey;
  }

  public void setMetaKey(String metaKey) {
    this.metaKey = metaKey;
  }

  public Integer getValueIndex() {
    return valueIndex;
  }

  public void setValueIndex(Integer valueIndex) {
    this.valueIndex = valueIndex;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }
}
