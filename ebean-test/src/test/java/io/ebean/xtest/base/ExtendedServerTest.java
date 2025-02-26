package io.ebean.xtest.base;

import io.ebean.*;
import io.ebean.annotation.Platform;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasicVer;
import org.tests.model.basic.ResetBasicData;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExtendedServerTest extends BaseTestCase {

  @Test
  void findList() {
    ResetBasicData.reset();

    Database server = DB.getDefault();

    Query<Customer> query = server.find(Customer.class)
      .where().startsWith("name", "Rob")
      .query();

    // rather than use .findList() return the query
    // when we use findList() .. it obtains a transaction using
    // the normal mechanism

    // obtain a transaction somehow ...
    // for this test/example we just begin one
    try (Transaction transaction = server.beginTransaction()) {

      // obtain extended API ... such that we can execute the
      // query using an explicit transaction
      List<Customer> customers = query.usingTransaction(transaction).findList();

      assertThat(customers).isNotEmpty();
      transaction.commit();
    }
  }

  @ForPlatform(Platform.H2)
  @Test
  void fixedClock() {
    final Instant now = Instant.now();
    Instant backedSnapshot = now.minus(1, ChronoUnit.DAYS);
    Clock snapshotClock = Clock.fixed(backedSnapshot, Clock.systemUTC().getZone());

    Database db = Database.builder()
      .name("db")
      .loadFromProperties()
      .clock(snapshotClock)
      .addClass(EBasicVer.class)
      .ddlExtra(false)
      .name("fixed-clock-db")
      .register(false)
      .defaultDatabase(false)
      .build();

    EBasicVer e0 = new EBasicVer("CheckClock");
    db.save(e0);

    EBasicVer found = db.find(EBasicVer.class, e0.getId());
    assertThat(found).isNotNull();

    int count = db
      .find(EBasicVer.class)
      .where()
      .gt("lastUpdate", now)
      .findCount();
    assertThat(count).isEqualTo(0);

    int count2 = db
      .find(EBasicVer.class)
      .where()
      .ge("lastUpdate", snapshotClock.instant().minusSeconds(60))
      .findCount();
    assertThat(count2).isEqualTo(1);

    int count3 = db
      .find(EBasicVer.class)
      .where()
      .gt("lastUpdate", now)
      .findCount();
    assertThat(count3).isEqualTo(0);

    int count4 = db
      .find(EBasicVer.class)
      .where()
      .ge("lastUpdate", backedSnapshot.minus(2, ChronoUnit.DAYS))
      .findCount();
    assertThat(count4).isEqualTo(1);
  }

}
