package org.tests.model.history;

import org.tests.model.draftable.BaseDomain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class HistorylessOneToOne extends BaseDomain {

  private final String name;

  /**
   * This @OneToOne(mappedBy=...) side should really be FetchType.LAZY
   */
  @OneToOne(mappedBy = "historylessOneToOne", cascade = CascadeType.ALL, orphanRemoval = true)//, fetch = FetchType.LAZY)
  HistoryOneToOne historyOneToOne;

  public HistorylessOneToOne(final String name) {
    this.name = name;
  }

  public HistoryOneToOne getHistoryOneToOne() {
    return historyOneToOne;
  }

  public void setHistoryOneToOne(final HistoryOneToOne historyOneToOne) {
    this.historyOneToOne = historyOneToOne;
  }

  public String getName() {
    return name;
  }
}
