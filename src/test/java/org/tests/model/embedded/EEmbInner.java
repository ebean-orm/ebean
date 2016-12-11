package org.tests.model.embedded;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "eemb_inner")
public class EEmbInner {

  @Id
  Integer id;

  String nomeInner;

  @Version
  private int updateCount;

  @ManyToOne
  EEmbOuter outer;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getNomeInner() {
    return nomeInner;
  }

  public void setNomeInner(String nomeInner) {
    this.nomeInner = nomeInner;
  }

  public EEmbOuter getOuter() {
    return outer;
  }

  public void setOuter(EEmbOuter outer) {
    this.outer = outer;
  }

  public int getUpdateCount() {
    return updateCount;
  }

  public void setUpdateCount(int updateCount) {
    this.updateCount = updateCount;
  }
}
