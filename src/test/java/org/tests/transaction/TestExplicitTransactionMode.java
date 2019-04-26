package org.tests.transaction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.BaseTestCase;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.config.ServerConfig;
import io.ebean.config.properties.PropertiesLoader;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.datasource.DataSourcePool;
import io.ebean.datasource.pool.ConnectionPool;
import io.ebeaninternal.server.type.ScalarTypeLocalDate;
import org.junit.Test;
import org.tests.model.basic.UTDetail;
import org.tests.model.basic.UTMaster;

import java.io.IOException;
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

    ServerConfig config = new ServerConfig();
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

    EbeanServer ebeanServer = EbeanServerFactory.create(config);

    testJsonScalarType(ebeanServer);

    Query<UTMaster> query = ebeanServer.find(UTMaster.class);
    List<UTMaster> details = query.findList();
    assertEquals(0, details.size());

    UTMaster bean0 = new UTMaster("one0");
    Transaction txn0 = ebeanServer.beginTransaction();
    try {
      ebeanServer.save(bean0);
      txn0.rollback();
    } finally {
      txn0.end();
    }

    // rollback as expected
    assertEquals(0, ebeanServer.find(UTMaster.class).findCount());

    UTMaster bean1 = new UTMaster("one1");
    UTMaster bean2 = new UTMaster("two2");
    UTMaster bean3 = new UTMaster("three3");

    // use a different transaction to do final query check
    try (Transaction otherTxn = ebeanServer.createTransaction()) {

      Transaction txn = ebeanServer.beginTransaction();
      try {
        ebeanServer.save(bean1);
        ebeanServer.save(bean2);

        // not visible in other transaction
        Query<UTMaster> query2 = ebeanServer.find(UTMaster.class);
        details = ebeanServer.extended().findList(query2, otherTxn);
        assertEquals(0, details.size());

        ebeanServer.save(bean3);

        txn.commit();

      } finally {
        txn.end();
      }

      // commit as expected
      Query<UTMaster> query3 = ebeanServer.find(UTMaster.class);
      details = ebeanServer.extended().findList(query3, otherTxn);
      assertEquals(3, details.size());
    }
  }

  private void testJsonScalarType(EbeanServer ebeanServer) {
    UTMaster bean = new UTMaster("one1");
    bean.setDate(LocalDate.of(2019, 04, 20));

    String json = ebeanServer.json().toJson(bean);
    assertThat(json).isEqualTo("{\"name\":\"one1\",\"date\":\"2019-04-20\"}");
    UTMaster jsonMaster = ebeanServer.json().toBean(UTMaster.class, json);
    assertThat(jsonMaster.getDate()).isEqualTo(LocalDate.of(2019, 4, 20));
  }

  public static class ScalarTypeLocalDateAsString extends ScalarTypeLocalDate {

    @Override
    public LocalDate jsonRead(JsonParser parser) throws IOException {
      return super.jsonRead(parser);
    }

    @Override
    public void jsonWrite(JsonGenerator writer, LocalDate value) throws IOException {
      writer.writeString(value.toString());
    }
  }
}
