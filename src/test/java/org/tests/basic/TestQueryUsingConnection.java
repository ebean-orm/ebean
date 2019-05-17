package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryUsingConnection extends BaseTestCase {

  @Test
  public void usingConnection() throws SQLException {

    ResetBasicData.reset();

    DataSource dataSource = DB.getDefault().getPluginApi().getDataSource();

    try (Connection connection = dataSource.getConnection()) {

      int count = DB.find(Country.class)
        .usingConnection(connection)
        .findCount();

      assertThat(count).isGreaterThan(0);
    }
  }

  @IgnorePlatform(Platform.SQLSERVER)
  @Test
  public void usingTransaction() {

    ResetBasicData.reset();

    try (Transaction transaction = DB.getDefault().createTransaction()) {

      Transaction current = Transaction.current();
      assertThat(current).isNull();

      Country x = new Country();
      x.setCode("xx");
      x.setName("WillRollThisBack");

      DB.getDefault().insert(x, transaction);

      final int count = DB.find(Country.class)
        .usingTransaction(transaction)
        .findCount();

      final int otherCount = DB.find(Country.class).findCount();

      transaction.rollback();

      assertThat(count).isEqualTo(otherCount + 1);
    }

  }
}
