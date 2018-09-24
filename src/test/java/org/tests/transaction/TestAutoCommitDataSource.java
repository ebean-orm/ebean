package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.config.ServerConfig;
import io.ebean.config.properties.PropertiesLoader;
import org.avaje.datasource.DataSourceConfig;
import org.avaje.datasource.DataSourcePool;
import org.avaje.datasource.pool.ConnectionPool;
import org.junit.Test;
import org.tests.model.basic.UTDetail;
import org.tests.model.basic.UTMaster;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestAutoCommitDataSource extends BaseTestCase {

  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  public void test() throws SQLException {

    Properties properties = PropertiesLoader.load();

    DataSourceConfig dsConfig = new DataSourceConfig();
    dsConfig.loadSettings(properties, "h2autocommit");//"pg"
    dsConfig.setAutoCommit(true);

    DataSourcePool pool = new ConnectionPool("h2autocommit", dsConfig);

    Connection connection = pool.getConnection();
    assertTrue(connection.getAutoCommit());
    connection.close();

    System.setProperty("ebean.ignoreExtraDdl", "true");

    ServerConfig config = new ServerConfig();
    config.setName("h2autocommit");
    config.loadFromProperties();
    config.setDataSource(pool);
    config.setDefaultServer(false);
    config.setRegister(false);

    config.addClass(UTMaster.class);
    config.addClass(UTDetail.class);
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setAutoCommitMode(true);

    EbeanServer ebeanServer = EbeanServerFactory.create(config);

    Query<UTMaster> query = ebeanServer.find(UTMaster.class);
    List<UTMaster> details = query.findList();
    assertEquals(0, details.size());

    UTMaster bean1 = new UTMaster("one1");
    UTMaster bean2 = new UTMaster("two2");
    UTMaster bean3 = new UTMaster("three3");

    // use a different transaction to do final query check
    try (Transaction otherTxn = ebeanServer.createTransaction()) {

      Transaction txn = ebeanServer.beginTransaction();
      try {
        assertTrue(txn.getConnection().getAutoCommit());
        ebeanServer.save(bean1);
        ebeanServer.save(bean2);

        Query<UTMaster> query2 = ebeanServer.find(UTMaster.class);
        details = ebeanServer.extended().findList(query2, otherTxn);
        assertEquals(2, details.size());

        ebeanServer.save(bean3);

        txn.rollback();

      } finally {
        txn.end();
      }

      Query<UTMaster> query3 = ebeanServer.find(UTMaster.class);
      details = ebeanServer.extended().findList(query3, otherTxn);
      assertEquals(3, details.size());
    }

  }
}
