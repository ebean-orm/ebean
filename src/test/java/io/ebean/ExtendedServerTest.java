package io.ebean;

import org.junit.After;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtendedServerTest extends BaseTestCase {

  @After
  public void cleanup() {
    Ebean.getDefaultServer()
      .extended()
      .setClock(Clock.systemUTC());
  }

  @Test
  public void findList() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getDefaultServer();

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
      List<Customer> customers = server.extended().findList(query, transaction);

      assertThat(customers).isNotEmpty();
      transaction.commit();
    }
  }

  @Test
  public void mockClock() {

    EbeanServer server = Ebean.getDefaultServer();
    final Instant snapshot = Instant.now();
    Instant backedSnapshot = snapshot.minus(1, ChronoUnit.DAYS);
    Clock snapshotClock = Clock.fixed(backedSnapshot, Clock.systemUTC().getZone());

    server.extended().setClock(snapshotClock);

    ResetBasicData.reset();

    int count = server
      .find(Customer.class)
      .where()
      .gt("cretime", snapshot)
      .findCount();
    assertThat(count).isEqualTo(0);

    int count2 = server
      .find(Customer.class)
      .where()
      .ge("cretime", backedSnapshot)
      .findCount();
    assertThat(count2).isGreaterThan(0);

    int count3 = server
      .find(Customer.class)
      .where()
      .gt("updtime", snapshot)
      .findCount();
    assertThat(count3).isEqualTo(0);

    int count4 = server
      .find(Customer.class)
      .where()
      .ge("updtime", backedSnapshot)
      .findCount();
    assertThat(count4).isGreaterThan(0);


  }

}
