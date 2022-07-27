package org.tests.o2m.dm;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import io.ebean.annotation.WhoCreated;
import io.ebean.annotation.WhoModified;
import org.tests.model.draftable.BaseDomain;

import javax.persistence.*;
import java.time.LocalDateTime;

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
