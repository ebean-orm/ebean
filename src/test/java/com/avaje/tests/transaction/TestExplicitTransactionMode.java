package com.avaje.tests.transaction;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.lib.sql.DataSourcePool;
import com.avaje.tests.model.basic.UTDetail;
import com.avaje.tests.model.basic.UTMaster;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestExplicitTransactionMode extends BaseTestCase {

  @Test
  public void test() throws SQLException {

    DataSourceConfig dsConfig = new DataSourceConfig();
    dsConfig.loadSettings("h2autocommit");//"h2autocommit","pg"
    dsConfig.setAutoCommit(true);

    DataSourcePool pool = new DataSourcePool(null, "h2autocommit", dsConfig);

    Connection connection = pool.getConnection();
    assertTrue(connection.getAutoCommit());
    connection.close();

    ServerConfig config = new ServerConfig();
    config.setName("h2autocommit");
    config.loadFromProperties();
    config.setDataSource(pool);
    config.setDefaultServer(false);
    config.setRegister(false);
    config.setExplicitTransactionBeginMode(true);

    config.addClass(UTMaster.class);
    config.addClass(UTDetail.class);
    config.setDdlGenerate(true);
    config.setDdlRun(true);

    EbeanServer ebeanServer = EbeanServerFactory.create(config);

    Query<UTMaster> query = ebeanServer.find(UTMaster.class);
    List<UTMaster> details = ebeanServer.findList(query, null);
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
    assertEquals(0, ebeanServer.find(UTMaster.class).findRowCount());

    UTMaster bean1 = new UTMaster("one1");
    UTMaster bean2 = new UTMaster("two2");
    UTMaster bean3 = new UTMaster("three3");

    // use a different transaction to do final query check
    Transaction otherTxn = ebeanServer.createTransaction();
    Transaction txn = ebeanServer.beginTransaction();

    try {
      ebeanServer.save(bean1);
      ebeanServer.save(bean2);

      // not visible in other transaction
      Query<UTMaster> query2 = ebeanServer.find(UTMaster.class);
      details = ebeanServer.findList(query2, otherTxn);
      assertEquals(0, details.size());

      ebeanServer.save(bean3);

      txn.commit();

    } finally {
      txn.end();
    }

    // commit as expected
    Query<UTMaster> query3 = ebeanServer.find(UTMaster.class);
    details = ebeanServer.findList(query3, otherTxn);
    assertEquals(3, details.size());
  }
}
