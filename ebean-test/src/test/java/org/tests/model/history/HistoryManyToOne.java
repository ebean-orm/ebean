package org.tests.model.history;

import io.ebean.annotation.History;
import io.ebean.annotation.SoftDelete;
import org.tests.model.draftable.BaseDomain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@History
@Entity
public class HistoryManyToOne extends BaseDomain {

  final String name;

  @OneToMany(cascade = CascadeType.REFRESH)
  List<HistorylessOneToOne> historylessOneToOne;

  @SoftDelete
  boolean deleted = false;

  public HistoryManyToOne(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public List<HistorylessOneToOne> getHistorylessOneToOne() {
    return historylessOneToOne;
  }

  public void setHistorylessOneToOne(List<HistorylessOneToOne> historylessOneToOne) {
    this.historylessOneToOne = historylessOneToOne;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}
