package org.tests.model.history;

import org.tests.model.draftable.BaseDomain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class HistorylessOneToOne extends BaseDomain {

  private final String name;

  /**
   * This @OneToOne(mappedBy=...) side should really be FetchType.LAZY
   */
  @OneToOne(mappedBy = "historylessOneToOne", cascade = CascadeType.ALL, orphanRemoval = true)//, fetch = FetchType.LAZY)
  HistoryOneToOne historyOneToOne;

  @ManyToOne(cascade = CascadeType.ALL)
  HistoryManyToOne historyManyToOne;

  public HistorylessOneToOne(final String name) {
    this.name = name;
  }

  public HistoryOneToOne getHistoryOneToOne() {
    return historyOneToOne;
  }

  public void setHistoryOneToOne(final HistoryOneToOne historyOneToOne) {
    this.historyOneToOne = historyOneToOne;
  }

  public HistoryManyToOne getHistoryManyToOne() {
    return historyManyToOne;
  }

  public void setHistoryManyToOne(final HistoryManyToOne historyManyToOne) {
    this.historyManyToOne = historyManyToOne;
  }

  public String getName() {
    return name;
  }
}
