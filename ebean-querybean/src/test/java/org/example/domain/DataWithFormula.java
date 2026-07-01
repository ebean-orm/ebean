package org.example.domain;

import jakarta.persistence.*;

@Entity
public class DataWithFormula {
  @EmbeddedId
  private DataWithFormulaKey id;

  @Column(insertable = false, updatable = false)
  private String metaKey;
  @Column(insertable = false, updatable = false)
  private Integer valueIndex;
  @ManyToOne
  private DataWithFormulaMain main;

  public DataWithFormulaKey getId() {
    return id;
  }

  public void setId(DataWithFormulaKey id) {
    this.id = id;
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
}
