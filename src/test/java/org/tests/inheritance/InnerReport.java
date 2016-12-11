package org.tests.inheritance;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

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
  @DiscriminatorValue("F")
  public static class Forecast extends Stockforecast {

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
