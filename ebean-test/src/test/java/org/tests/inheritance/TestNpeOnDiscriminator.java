package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

public class TestNpeOnDiscriminator extends BaseTestCase {

  @Test
  public void test() {

    InnerReport report = DB.json().toBean(InnerReport.class, "{}");
    DB.save(report);

    // other service ...
    InnerReport.Forecast f = new InnerReport.Forecast();
    report.setForecast(f);
    f.innerReport = report;

    DB.save(f);

  }
}
