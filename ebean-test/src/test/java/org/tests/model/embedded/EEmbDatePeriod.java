package org.tests.model.embedded;

import io.ebean.annotation.NotNull;

import javax.persistence.Embeddable;
import java.util.Date;

@Embeddable
public class EEmbDatePeriod {

  @NotNull
  Date date1;
  @NotNull
  Date date2;

  public Date getDate1() {
    return date1;
  }

  public void setDate1(Date date1) {
    this.date1 = date1;
  }

  public Date getDate2() {
    return date2;
  }

  public void setDate2(Date date2) {
    this.date2 = date2;
  }
}
