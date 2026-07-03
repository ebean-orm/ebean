package org.tests.model.history;

import io.ebean.annotation.History;
import io.ebean.annotation.SoftDelete;
import jakarta.persistence.Entity;
import org.tests.model.draftable.BaseDomain;

@History
@Entity
public class HistoryManyToOne extends BaseDomain {

  final String name;

  @SoftDelete
  boolean deleted;

  public HistoryManyToOne(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}
