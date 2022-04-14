package org.tests.model.history;

import io.ebean.annotation.History;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import org.tests.model.draftable.BaseDomain;

@History
@Entity
public class HistoryOneToOne extends BaseDomain {

  String name;

  @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.REFRESH})
  HistorylessOneToOne historylessOneToOne;

  public HistoryOneToOne(String name) {
    this.name = name;
  }

  public HistoryOneToOne() {
  }

  public HistorylessOneToOne getHistorylessOneToOne() {
    return historylessOneToOne;
  }

  public void setHistorylessOneToOne(final HistorylessOneToOne historylessOneToOne) {
    this.historylessOneToOne = historylessOneToOne;
  }
}
