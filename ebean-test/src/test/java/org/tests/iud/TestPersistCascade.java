package org.tests.iud;

import io.avaje.moduuid.ModUUID;
import io.ebean.xtest.BaseTestCase;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

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

    LoggedSql.start();
    country.save();

    final List<String> sql = LoggedSql.stop();

    if (isPersistBatchOnCascade()) {
      assertSql(sql.get(0)).contains("insert into pcf_country");
      assertSql(sql.get(1)).contains("insert into pcf_person");
      assertSqlBind(sql, 2, 7);
      assertThat(sql.get(9)).contains("insert into pcf_calendar");
      assertSqlBind(sql, 10, 21);
      assertThat(sql.get(23)).contains("insert into pcf_city");
      assertSqlBind(sql, 24, 26);
      assertThat(sql.get(28)).contains("insert into pcf_event");
      assertSqlBind(sql, 29, 48);
      assertThat(sql.get(50)).contains("insert into pcf_event");
      assertSqlBind(sql, 51, 70);
      assertThat(sql.get(72)).contains("insert into pcf_event");
      assertSqlBind(sql, 73, 87);
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
