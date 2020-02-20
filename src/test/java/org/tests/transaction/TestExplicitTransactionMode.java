package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.JsonConfig;
import io.ebean.config.properties.PropertiesLoader;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.datasource.DataSourcePool;
import io.ebean.datasource.pool.ConnectionPool;
import io.ebeaninternal.server.type.ScalarTypeLocalDate;
import org.junit.Test;
import org.tests.model.basic.UTDetail;
import org.tests.model.basic.UTMaster;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestExplicitTransactionMode extends BaseTestCase {

  @ForPlatform(Platform.H2)
  @Test
  public void test() throws SQLException {

    Properties properties = PropertiesLoader.load();

    DataSourceConfig dsConfig = new DataSourceConfig();
    dsConfig.loadSettings(properties, "h2autocommit2");
    dsConfig.setAutoCommit(true);

    DataSourcePool pool = new ConnectionPool("h2autocommit2", dsConfig);

    Connection connection = pool.getConnection();
    assertTrue(connection.getAutoCommit());
    connection.close();

    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2autocommit2");
    config.loadFromProperties();
    config.setDataSource(pool);
    config.setDefaultServer(false);
    config.setRegister(false);
    config.setExplicitTransactionBeginMode(true);

    config.addClass(UTMaster.class);
    config.addClass(UTDetail.class);
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);
    config.addClass(ScalarTypeLocalDateAsString.class);

    Database database = DatabaseFactory.create(config);

    testJsonScalarType(database);

    Query<UTMaster> query = database.find(UTMaster.class);
    List<UTMaster> details = query.findList();
    assertEquals(0, details.size());

    UTMaster bean0 = new UTMaster("one0");
    Transaction txn0 = database.beginTransaction();
    try {
      database.save(bean0);
      txn0.rollback();
    } finally {
      txn0.end();
    }

    // rollback as expected
    assertEquals(0, database.find(UTMaster.class).findCount());

    UTMaster bean1 = new UTMaster("one1");
    UTMaster bean2 = new UTMaster("two2");
    UTMaster bean3 = new UTMaster("three3");

    // use a different transaction to do final query check
    try (Transaction otherTxn = database.createTransaction()) {

      Transaction txn = database.beginTransaction();
      try {
        database.save(bean1);
        database.save(bean2);

        // not visible in other transaction
        Query<UTMaster> query2 = database.find(UTMaster.class);
        details = database.extended().findList(query2, otherTxn);
        assertEquals(0, details.size());

        database.save(bean3);

        txn.commit();

      } finally {
        txn.end();
      }

      // commit as expected
      details = database.find(UTMaster.class)
        .usingTransaction(otherTxn)
        .findList();

      assertEquals(3, details.size());
    }
  }

  private void testJsonScalarType(Database ebeanServer) {
    UTMaster bean = new UTMaster("one1");
    bean.setEventDate(LocalDate.of(2019, 04, 20));

    String json = ebeanServer.json().toJson(bean);
    assertThat(json).isEqualTo("{\"name\":\"one1\",\"eventDate\":\"2019-04-20\"}");
    UTMaster jsonMaster = ebeanServer.json().toBean(UTMaster.class, json);
    assertThat(jsonMaster.getEventDate()).isEqualTo(LocalDate.of(2019, 4, 20));
  }

  public static class ScalarTypeLocalDateAsString extends ScalarTypeLocalDate {

    public ScalarTypeLocalDateAsString() {
      super(JsonConfig.Date.ISO8601);
    }
  }

  @Test
  public void modelSaveWithTransaction() {

    try (Transaction txn = DB.getDefault().createTransaction()) {

      UTMaster bean1 = new UTMaster("otherSave");
      bean1.save(txn);
      assertThat(DB.find(UTMaster.class).usingTransaction(txn)
        .where().eq("name", "otherSave").findOne()).isNotNull();

      bean1.deletePermanent(txn);
      assertThat(DB.find(UTMaster.class).usingTransaction(txn)
        .where().eq("name", "otherSave").findOne())
        .isNull();

      UTMaster bean2 = new UTMaster("otherInsert");
      bean2.insert(txn);
      assertTrue(DB.find(UTMaster.class).usingTransaction(txn)
        .where().eq("name", "otherInsert").exists());

      bean2.setDescription("changed description");
      bean2.update(txn);

      assertThat(DB.find(UTMaster.class).usingTransaction(txn)
        .where().eq("name", "otherInsert").findOne())
        .isNotNull()
        .extracting(UTMaster::getDescription).contains("changed description");

      UTMaster bean3 = new UTMaster("otherThree");
      bean3.save(txn);

      assertThat(DB.find(UTMaster.class).usingTransaction(txn)
        .where().eq("name", "otherThree").findOne())
        .isNotNull();

      bean3.delete(txn);

      assertThat(DB.find(UTMaster.class).usingTransaction(txn)
        .where().eq("name", "otherThree").findOne())
        .isNull();
    }
  }
}
