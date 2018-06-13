package io.ebean;

import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtendedServerTest extends BaseTestCase {

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

}
