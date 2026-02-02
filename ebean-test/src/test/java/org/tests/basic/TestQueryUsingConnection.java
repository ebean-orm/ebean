package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQueryUsingConnection extends BaseTestCase {

  @Test
  public void usingConnection() throws SQLException {

    ResetBasicData.reset();

    DataSource dataSource = DB.getDefault().dataSource();

    try (Connection connection = dataSource.getConnection()) {

      int count = DB.find(Country.class)
        .usingConnection(connection)
        .findCount();

      assertThat(count).isGreaterThan(0);
      connection.rollback();
    }
  }

  @IgnorePlatform({Platform.SQLSERVER, Platform.COCKROACH})
  @Test
  public void usingTransaction() {

    ResetBasicData.reset();

    try (Transaction transaction = DB.createTransaction()) {

      Transaction current = Transaction.current();
      assertThat(current).isNull();

      Country x = new Country();
      x.setCode("xx");
      x.setName("WillRollThisBack");

      DB.getDefault().insert(x, transaction);

      final CountryDto dto = DB.findDto(CountryDto.class, "select code, name from o_country where code=?")
        .usingTransaction(transaction)
        .setParameter(1, "xx")
        .findOne();

      assertThat(dto).isNotNull();
      assertEquals("xx", dto.code);
      assertEquals("WillRollThisBack", dto.name);

      final int count = DB.find(Country.class)
        .usingTransaction(transaction)
        .findCount();

      final int otherCount = DB.find(Country.class).findCount();
      final int masterCount = DB.find(Country.class).usingMaster().findCount();
      final int masterCount2 = DB.find(Country.class).usingMaster(true).findCount();
      final int masterCount3 = DB.find(Country.class).usingMaster(false).findCount();

      transaction.rollback();

      assertThat(count).isEqualTo(otherCount + 1);
      assertThat(otherCount).isEqualTo(masterCount);
      assertThat(otherCount).isEqualTo(masterCount2);
      assertThat(otherCount).isEqualTo(masterCount3);
    }
  }

  @IgnorePlatform({Platform.SQLSERVER, Platform.COCKROACH})
  @Test
  public void dtoQueryUsingConnection() {
    ResetBasicData.reset();

    try (Transaction transaction = DB.createTransaction()) {
      final CountryDto dto = DB.findDto(CountryDto.class, "select code, name from o_country where code=?")
        .usingConnection(transaction.connection())
        .setParameter("NZ")
        .findOne();

      assertThat(dto).isNotNull();
      assertEquals("NZ", dto.code);
      transaction.rollback();
    }
  }


  public static class CountryDto {
    final String code;
    final String name;

    public CountryDto(String code, String name) {
      this.code = code;
      this.name = name;
    }
  }
}
