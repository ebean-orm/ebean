package io.ebean.config;


import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.datasource.DataSourceAlert;
import io.ebean.datasource.DataSourceInitialiseException;

import org.junit.Test;
import org.tests.model.basic.EBasicVer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThatThrownBy;

public class TestServerOffline {

  @Test
  @ForPlatform({Platform.H2})
  public void testOffline_with_failOnStart() throws SQLException {

    String url = "jdbc:h2:mem:testoffline1";
    try (Connection bootup = DriverManager.getConnection(url, "sa", "secret")) {
      Properties props = props(url);
      ServerConfig config = config(props);
      assertThatThrownBy(() -> EbeanServerFactory.create(config))
        .isInstanceOf(DataSourceInitialiseException.class);
   }

  }

  @Test
  @ForPlatform({Platform.H2})
  public void testOffline_no_failOnStart() throws SQLException {

    String url = "jdbc:h2:mem:testoffline2";
    try (Connection bootup = DriverManager.getConnection(url, "sa", "secret")) {
      Properties props = props(url);
      props.setProperty("datasource.h2_offline.failOnStart", "false");
      ServerConfig config = config(props);
      EbeanServer h2Offline = EbeanServerFactory.create(config);
      assertThat(h2Offline).isNotNull();
      assertThat(h2Offline.isStarted()).isFalse();
    }
  }

  private static class MyDataSourceAlert implements DataSourceAlert {

    public EbeanServer server;

    @Override
    public void dataSourceUp(DataSource dataSource) {
      if (!server.isStarted()) {
        System.out.println("Starting the server");
        server.start();
      }
    }

    @Override
    public void dataSourceDown(DataSource dataSource, SQLException reason) {}

    @Override
    public void dataSourceWarning(DataSource dataSource, String msg) {}

  }
  @Test
  @ForPlatform({Platform.H2})
  public void testOffline_recovery() throws SQLException, InterruptedException {

    String url = "jdbc:h2:mem:testoffline3";
    try (Connection bootup = DriverManager.getConnection(url, "sa", "secret")) {

      Properties props = props(url);
      props.setProperty("datasource.h2_offline.failOnStart", "false");
      props.setProperty("ebean.h2_offline.ddl.generate", "true");
      props.setProperty("ebean.h2_offline.ddl.run", "true");
      MyDataSourceAlert alert = new MyDataSourceAlert() ;

      ServerConfig config = config(props);

      config.getDataSourceConfig().setAlert(alert);
      config.getDataSourceConfig().setHeartbeatFreqSecs(1);

      EbeanServer h2Offline = EbeanServerFactory.create(config);
      alert.server = h2Offline;
      assertThat(h2Offline).isNotNull();
      assertThat(h2Offline.isStarted()).isFalse();

      // so - reset the password so that the server can reconnect
      try (Statement stmt = bootup.createStatement()) {
        stmt.execute("alter user sa set password 'sa'");
      }
      // wait up to 10 seconds until the DataSourcePool detects, that DS is up now...
      int i = 0;
      System.out.println("Waiting for DB-reconnect");
      while (!h2Offline.isStarted() && i++ < 100) {
        Thread.sleep(100);
      }
      assertThat(h2Offline.isStarted()).isTrue();

      // check if server is working (i.e. ddl did run)
      EBasicVer bean = new EBasicVer("foo");
      h2Offline.save(bean);
      assertThat(h2Offline.find(EBasicVer.class).findCount()).isEqualTo(1);
      h2Offline.delete(bean);

      System.out.println("Done");
    }
  }

  private Properties props(String url) {

    Properties props = new Properties();

    props.setProperty("datasource.h2_offline.username", "sa");
    props.setProperty("datasource.h2_offline.password", "sa");
    props.setProperty("datasource.h2_offline.url", url);
    props.setProperty("datasource.h2_offline.driver", "org.h2.Driver");
    props.setProperty("ebean.h2_offline.databasePlatformName", "h2");
    props.setProperty("ebean.h2_offline.ddl.extra", "false");

    return props;
  }

  private ServerConfig config(Properties props) {
    ServerConfig config = new ServerConfig();
    config.setName("h2_offline");
    config.loadFromProperties(props);
    config.setDefaultServer(false);
    config.setRegister(false);
    config.getClasses().add(EBasicVer.class);
    // beans required to execute DDL
//    config.getClasses().add(Address.class);
//    config.getClasses().add(Customer.class);
//    config.getClasses().add(Country.class);
//    config.getClasses().add(Order.class);
//    config.getClasses().add(OrderShipment.class);
//    config.getClasses().add(OrderDetail.class);
//    config.getClasses().add(Product.class);
    return config;
  }

}
