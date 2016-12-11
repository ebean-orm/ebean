package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

public class TestNpeOnDiscriminator extends BaseTestCase {

  @Test
  public void test() {

    InnerReport report = Ebean.json().toBean(InnerReport.class, "{}");
    Ebean.save(report);

    // other service ...
    InnerReport.Forecast f = new InnerReport.Forecast();
    report.setForecast(f);
    f.innerReport = report;

    Ebean.save(f);

  }
}
