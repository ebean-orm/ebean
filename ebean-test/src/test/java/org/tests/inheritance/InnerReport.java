package org.tests.inheritance;

import javax.persistence.*;

/**
 */
@Entity
public class InnerReport {

  @Id
  Long id;

  String name;

  @OneToOne
  Forecast forecast;

  public Forecast getForecast() {
    return forecast;
  }

  public void setForecast(Forecast forecast) {
    this.forecast = forecast;
  }


  @Entity
  public static class Forecast {

    @Id
    long id;

    @ManyToOne
    InnerReport innerReport;

    public InnerReport getInnerReport() {
      return innerReport;
    }

    public void setInnerReport(InnerReport innerReport) {
      this.innerReport = innerReport;
    }
  }

}
