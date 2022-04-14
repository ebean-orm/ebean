package org.tests.model.history;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import org.tests.model.draftable.BaseDomain;

@Entity
public class HistorylessOneToOne extends BaseDomain {
  private String name;

  public HistorylessOneToOne() {}

  public HistorylessOneToOne(final String name) {
    this.name = name;
  }

  @OneToOne(mappedBy = "historylessOneToOne", cascade = CascadeType.ALL, orphanRemoval = true)
  HistoryOneToOne historyOneToOne;

  public HistoryOneToOne getHistoryOneToOne() {
    return historyOneToOne;
  }

  public void setHistoryOneToOne(final HistoryOneToOne historyOneToOne) {
    this.historyOneToOne = historyOneToOne;
  }
}
