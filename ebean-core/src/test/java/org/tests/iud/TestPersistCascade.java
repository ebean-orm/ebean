package org.tests.iud;

import io.avaje.moduuid.ModUUID;
import io.ebean.BaseTestCase;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPersistCascade extends BaseTestCase {

  @Test
  public void insert() {

    PcfCountry country = new PcfCountry();
    for (int a = 0; a < 3; a++) {
      PcfPerson mayor = createPerson();
      PcfPerson viceMayor = createPerson();
      country.addCity(new PcfCity("city_" + ModUUID.newShortId(), mayor, viceMayor));
    }

    LoggedSqlCollector.start();
    country.save();

    final List<String> sql = LoggedSqlCollector.stop();

    if (isPersistBatchOnCascade()) {
      assertSql(sql.get(0)).contains("insert into pcf_country");
      assertSql(sql.get(1)).contains("insert into pcf_person");
      assertSqlBind(sql, 2, 7);
      assertThat(sql.get(8)).contains("insert into pcf_calendar");
      assertSqlBind(sql, 9, 20);
      assertThat(sql.get(21)).contains("insert into pcf_city");
      assertSqlBind(sql, 22, 24);
      assertThat(sql.get(25)).contains("insert into pcf_event");
      assertSqlBind(sql, 26, 45);
      assertThat(sql.get(46)).contains("insert into pcf_event");
      assertSqlBind(sql, 47, 66);
      assertThat(sql.get(67)).contains("insert into pcf_event");
      assertSqlBind(sql, 68, 87);
    }

    country.deletePermanent();
  }

  private static PcfPerson createPerson() {
    PcfPerson person = new PcfPerson("person_" + ModUUID.newShortId());
    for (int a = 0; a < 2; a++) {
      PcfCalendar calendar = new PcfCalendar();
      for (int b = 0; b < 10; b++) {
        calendar.addEvent(new PcfEvent("evt_" + ModUUID.newShortId()));
      }
      person.addCalendar(calendar);
    }
    return person;
  }

}
