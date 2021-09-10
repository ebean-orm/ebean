package org.tests.model.embedded;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "eemb_outer")
public class EEmbOuter {
  @Id
  Integer id;
  String nomeOuter;
  @Version
  private int updateCount;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  List<EEmbInner> inners;

  @Embedded
  EEmbDatePeriod datePeriod;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getNomeOuter() {
    return nomeOuter;
  }

  public void setNomeOuter(String nomeOuter) {
    this.nomeOuter = nomeOuter;
  }

  public List<EEmbInner> getInners() {
    return inners;
  }

  public void setInners(List<EEmbInner> inners) {
    this.inners = inners;
  }

  public EEmbDatePeriod getDatePeriod() {
    return datePeriod;
  }

  public void setDatePeriod(EEmbDatePeriod datePeriod) {
    this.datePeriod = datePeriod;
  }

  public int getUpdateCount() {
    return updateCount;
  }

  public void setUpdateCount(int updateCount) {
    this.updateCount = updateCount;
  }
}
