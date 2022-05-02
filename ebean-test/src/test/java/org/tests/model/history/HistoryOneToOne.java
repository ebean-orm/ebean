package org.tests.model.history;

import io.ebean.annotation.History;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import org.tests.model.draftable.BaseDomain;

@History
@Entity
public class HistoryOneToOne extends BaseDomain {

  final String name;

  @OneToOne(cascade = CascadeType.REFRESH)
  HistorylessOneToOne historylessOneToOne;

  public HistoryOneToOne(String name) {
    this.name = name;
  }

  public HistorylessOneToOne less() {
    return historylessOneToOne;
  }

  public String getName() {
    return name;
  }
}
