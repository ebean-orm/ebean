package org.example.domain;

import io.ebean.annotation.NotNull;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DataWithFormulaKey {

  @NotNull
  private final UUID mainId;
  @NotNull
  private final String metaKey;
  @NotNull
  private final Integer valueIndex;

  public DataWithFormulaKey(UUID mainId, String metaKey, Integer valueIndex) {
    this.mainId = mainId;
    this.metaKey = metaKey;
    this.valueIndex = valueIndex;
  }

  public UUID getMainId() {
    return mainId;
  }

  public String getMetaKey() {
    return metaKey;
  }

  public Integer getValueIndex() {
    return valueIndex;
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
