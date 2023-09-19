package org.tests.model.composite;


import io.ebean.annotation.Formula;
import io.ebean.annotation.Index;
import jakarta.persistence.*;

@Entity
public class DataWithFormula {

  @EmbeddedId
  private DataWithFormulaKey id;

  // @Column(insertable = false, updatable = false)
  @Formula(select = "${ta}.meta_key")
  private String metaKey;

  // @Column(insertable = false, updatable = false)
  @Formula(select = "${ta}.value_index")
  private Integer valueIndex;

  @ManyToOne
  @JoinColumn(name = "main_id", insertable = false, nullable = false)
  private DataWithFormulaMain main;

  @Index
  private String stringValue;

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

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }
}
