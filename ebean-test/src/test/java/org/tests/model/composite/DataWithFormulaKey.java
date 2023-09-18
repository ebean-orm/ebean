package org.tests.model.composite;

import io.ebean.annotation.NotNull;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DataWithFormulaKey {

  private static final long serialVersionUID = 1L;

  @NotNull
  private UUID mainId;

  @NotNull
  private String metaKey = "";

  @NotNull
  private Integer valueIndex = 0;

  public UUID getMainId() {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataWithFormulaKey that = (DataWithFormulaKey) o;
    return Objects.equals(mainId, that.mainId) && Objects.equals(metaKey, that.metaKey) && Objects.equals(valueIndex, that.valueIndex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mainId, metaKey, valueIndex);
  }
}
