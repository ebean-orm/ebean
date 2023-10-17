package org.tests.o2m.dm;

import org.tests.model.draftable.BaseDomain;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class HistoryColumns extends BaseDomain {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private PersonEntity createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private PersonEntity updatedBy;


  public PersonEntity getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(PersonEntity createdBy) {
    this.createdBy = createdBy;
  }

  public PersonEntity getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(PersonEntity updatedBy) {
    this.updatedBy = updatedBy;
  }
}
