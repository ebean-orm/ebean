package org.tests.iud;

import io.avaje.moduuid.ModUUID;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
      assertSqlBind(sql, 29, 128);
      assertThat(sql.get(130)).contains("insert into pcf_event");
      assertSqlBind(sql, 131, 150);
    }

    country.deletePermanent();
  }

  @Test
  public void deletePermanent_groupsImportedRelationships() {
    PcfCountry control = countryWithCities(3, true);
    PcfCountry country = countryWithCities(3, true);

    LoggedSql.start();
    country.deletePermanent();
    List<String> sql = statements(LoggedSql.stop());

    // mayor and viceMayor share one Person delete; cities are not removed one by one
    assertThat(count(sql, "delete from pcf_person")).isEqualTo(1);
    assertThat(count(sql, "delete from pcf_city")).isEqualTo(1);
    assertThat(count(sql, "delete from pcf_calendar")).isEqualTo(1);
    assertThat(count(sql, "delete from pcf_event")).isEqualTo(1);
    assertThat(selects(sql, "pcf_calendar")).isEqualTo(1);
    assertThat(selects(sql, "pcf_city")).isEqualTo(2);
    assertVersionedCountryDelete(sql);

    assertGraph(country, false);
    assertGraph(control, true);
  }

  @Test
  public void deletePermanent_statementCountIndependentOfCityCount() {
    int oneCity = deleteStatementCount(1);
    int severalCities = deleteStatementCount(3);
    assertThat(oneCity).isGreaterThan(0).isEqualTo(severalCities);
  }

  @Test
  public void deletePermanent_savedLoadedAndReferenceRoots() {
    PcfCountry saved = countryWithCities(1, true);
    LoggedSql.start();
    saved.deletePermanent();
    assertVersionedCountryDelete(statements(LoggedSql.stop()));
    assertGraph(saved, false);

    PcfCountry toLoad = countryWithCities(1, true);
    PcfCountry loaded = DB.find(PcfCountry.class, toLoad.id);
    LoggedSql.start();
    loaded.deletePermanent();
    assertVersionedCountryDelete(statements(LoggedSql.stop()));
    assertGraph(toLoad, false);

    PcfCountry toReference = countryWithCities(1, true);
    DB.reference(PcfCountry.class, toReference.id).deletePermanent();
    assertGraph(toReference, false);
  }

  @Test
  public void deletePermanent_emptyCountryAndPersonWithoutCalendars() {
    PcfCountry empty = new PcfCountry();
    empty.save();
    LoggedSql.start();
    empty.deletePermanent();
    List<String> emptySql = statements(LoggedSql.stop());
    assertVersionedCountryDelete(emptySql);
    assertThat(count(emptySql, "delete from pcf_city")).isZero();
    assertThat(count(emptySql, "pcf_person")).isZero();
    assertThat(count(emptySql, "pcf_calendar")).isZero();
    assertThat(count(emptySql, "pcf_event")).isZero();
    assertThat(DB.find(PcfCountry.class, empty.id)).isNull();

    PcfCountry country = countryWithCities(1, false);
    LoggedSql.start();
    country.deletePermanent();
    List<String> sql = statements(LoggedSql.stop());
    assertVersionedCountryDelete(sql);
    assertThat(count(sql, "delete from pcf_person")).isEqualTo(1);
    assertThat(count(sql, "delete from pcf_city")).isEqualTo(1);
    assertThat(count(sql, "pcf_event")).isZero();
    assertThat(count(sql, "delete from pcf_calendar")).isZero();
    assertGraph(country, false);
  }

  @Test
  public void deleteAll_missingIdsAndBatchLimit() {
    PcfCountry control = countryWithCities(1, true);
    PcfPerson stray = new PcfPerson("stray_" + ModUUID.newShortId());
    stray.save();

    assertThat(DB.deleteAllPermanent(PcfCity.class, List.of(-1L, -2L))).isEqualTo(0);
    assertGraph(control, true);
    assertThat(DB.find(PcfPerson.class, stray.id)).isNotNull();

    PcfCountry target = countryWithCities(1, true);
    long cityId = this.<List<PcfCity>>field(target, "cities").get(0).id;
    int batch = maxDeleteBatch();
    List<Long> ids = new ArrayList<>(batch + 1);
    ids.add(cityId);
    for (int i = 0; i < batch; i++) {
      ids.add(-1_000L - i);
    }

    LoggedSql.start();
    int rows = DB.deleteAllPermanent(PcfCity.class, ids);
    List<String> sql = statements(LoggedSql.stop());

    assertThat(rows).isEqualTo(1);
    assertThat(selects(sql, "pcf_city")).isEqualTo(2);
    assertThat(count(sql, "delete from pcf_city")).isEqualTo(1);
    assertThat(count(sql, "delete from pcf_person")).isEqualTo(1);
    assertThat(DB.find(PcfCountry.class, target.id)).isNotNull();
    assertThat(DB.find(PcfCity.class, cityId)).isNull();
    assertPerson(this.<PcfPerson>field(this.<List<PcfCity>>field(target, "cities").get(0), "mayor"), false);
    assertPerson(this.<PcfPerson>field(this.<List<PcfCity>>field(target, "cities").get(0), "viceMayor"), false);
    assertGraph(control, true);
    assertThat(DB.find(PcfPerson.class, stray.id)).isNotNull();
  }

  @Test
  public void deletePermanent_rollbackAndStaleVersion() {
    PcfCountry rolledBack = countryWithCities(2, true);
    try (Transaction txn = DB.beginTransaction()) {
      rolledBack.deletePermanent();
      txn.rollback();
    }
    assertGraph(rolledBack, true);

    PcfCountry stale = countryWithCities(1, true);
    DB.sqlUpdate("update pcf_country set version = version + 1 where id = ?")
      .setParameter(stale.id)
      .execute();
    assertThrows(OptimisticLockException.class, stale::deletePermanent);
    assertGraph(stale, true);
  }

  private int deleteStatementCount(int cities) {
    PcfCountry country = countryWithCities(cities, true);
    LoggedSql.start();
    country.deletePermanent();
    return statements(LoggedSql.stop()).size();
  }

  private PcfCountry countryWithCities(int cities, boolean withCalendars) {
    PcfCountry country = new PcfCountry();
    for (int a = 0; a < cities; a++) {
      PcfPerson mayor = withCalendars ? createPerson() : new PcfPerson("person_" + ModUUID.newShortId());
      PcfPerson viceMayor = withCalendars ? createPerson() : new PcfPerson("person_" + ModUUID.newShortId());
      country.addCity(new PcfCity("city_" + ModUUID.newShortId(), mayor, viceMayor));
    }
    country.save();
    return country;
  }

  private void assertVersionedCountryDelete(List<String> sql) {
    assertThat(sql).anySatisfy(statement ->
      assertSql(statement).contains("delete from pcf_country").contains("version"));
  }

  private void assertGraph(PcfCountry country, boolean present) {
    if (present) {
      assertThat(DB.find(PcfCountry.class, country.id)).isNotNull();
    } else {
      assertThat(DB.find(PcfCountry.class, country.id)).isNull();
    }
    for (PcfCity city : this.<List<PcfCity>>field(country, "cities")) {
      if (present) {
        assertThat(DB.find(PcfCity.class, city.id)).isNotNull();
      } else {
        assertThat(DB.find(PcfCity.class, city.id)).isNull();
      }
      assertPerson(field(city, "mayor"), present);
      assertPerson(field(city, "viceMayor"), present);
    }
  }

  private void assertPerson(PcfPerson person, boolean present) {
    if (present) {
      assertThat(DB.find(PcfPerson.class, person.id)).isNotNull();
    } else {
      assertThat(DB.find(PcfPerson.class, person.id)).isNull();
    }
    // enhancement leaves an untouched OneToMany null until addCalendar/addEvent
    List<PcfCalendar> calendars = field(person, "calendars");
    if (calendars == null) {
      return;
    }
    for (PcfCalendar calendar : calendars) {
      if (present) {
        assertThat(DB.find(PcfCalendar.class, calendar.id)).isNotNull();
      } else {
        assertThat(DB.find(PcfCalendar.class, calendar.id)).isNull();
      }
      List<PcfEvent> events = field(calendar, "events");
      if (events == null) {
        continue;
      }
      for (PcfEvent event : events) {
        if (present) {
          assertThat(DB.find(PcfEvent.class, event.id)).isNotNull();
        } else {
          assertThat(DB.find(PcfEvent.class, event.id)).isNull();
        }
      }
    }
  }

  /**
   * Same cap as {@code DefaultPersister}: simple ids, so one parameter per id.
   */
  private int maxDeleteBatch() {
    int maxInBinding = spiEbeanServer().databasePlatform().maxInBinding();
    return maxInBinding == 0 ? 1000 : Math.min(1000, maxInBinding);
  }

  private static List<String> statements(List<String> sql) {
    List<String> statements = new ArrayList<>();
    for (String line : sql) {
      // LoggedSql lines are "txn[id] <sql>"; bind and executeBatch lines are not statements
      String trimmed = stripTxn(line).toLowerCase();
      if (trimmed.startsWith("select") || trimmed.startsWith("delete")
        || trimmed.startsWith("update") || trimmed.startsWith("insert")) {
        statements.add(line);
      }
    }
    return statements;
  }

  private static String stripTxn(String line) {
    String trimmed = line.trim();
    if (trimmed.regionMatches(true, 0, "txn[", 0, 4)) {
      int end = trimmed.indexOf(']');
      if (end > 0 && end + 1 < trimmed.length()) {
        return trimmed.substring(end + 1).trim();
      }
    }
    return trimmed;
  }

  private static int count(List<String> sql, String fragment) {
    int n = 0;
    for (String line : sql) {
      if (line.contains(fragment)) {
        n++;
      }
    }
    return n;
  }

  private static int selects(List<String> sql, String table) {
    int n = 0;
    for (String line : sql) {
      String lower = line.toLowerCase();
      if (lower.contains("select") && lower.contains(table)) {
        n++;
      }
    }
    return n;
  }

  @SuppressWarnings("unchecked")
  private <T> T field(Object bean, String name) {
    Class<?> type = bean.getClass();
    while (type != null) {
      try {
        Field found = type.getDeclaredField(name);
        found.setAccessible(true);
        return (T) found.get(bean);
      } catch (NoSuchFieldException e) {
        type = type.getSuperclass();
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }
    throw new IllegalStateException("No field " + name + " on " + bean.getClass());
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
