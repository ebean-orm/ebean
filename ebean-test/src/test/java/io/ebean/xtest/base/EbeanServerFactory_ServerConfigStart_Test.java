package io.ebean.xtest.base;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.event.ServerConfigStartup;
import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.api.SpiLoggerFactory;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.UTDetail;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EbeanServerFactory_ServerConfigStart_Test {


  /**
   * Demo, how to intercept logging for a certain database.
   * In conjunction to ServiceLoader, you can add different loggers to different databases
   */
  private static class MySpiLoggerFactory implements SpiLoggerFactory {
    Set<String> loggers = new HashSet<>();

    @Override
    public SpiLogger create(String name) {
      loggers.add(name);
      // just return a dummy here
      return new SpiLogger() {
        @Override
        public boolean isDebug() {
          return false;
        }

        @Override
        public void debug(String msg) {
        }
      };

    }
  }

  @Test
  public void test() throws InterruptedException {

    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2");
    config.loadFromProperties();
    config.setName("h2other");
    config.setDdlGenerate(false);
    config.setDdlRun(false);
    config.setDdlExtra(false);
    config.setDefaultServer(false);
    config.setRegister(false);

    config.addClass(UTDetail.class);
    config.addClass(OnStartupViaClass.class);

    // act - register an instance
    OnStartup onStartup = new OnStartup();
    config.addServerConfigStartup(onStartup);
    MySpiLoggerFactory loggerFactory = new MySpiLoggerFactory();
    config.putServiceObject(SpiLoggerFactory.class, loggerFactory);

    Database db = DatabaseFactory.create(config);

    assertThat(loggerFactory.loggers).containsExactlyInAnyOrder("io.ebean.SQL", "io.ebean.SUM", "io.ebean.TXN");

    assertThat(onStartup.calledWithConfig).isSameAs(config);
    assertThat(OnStartupViaClass.calledWithConfig).isSameAs(config);

    assertThat(db).isNotNull();

    // test server shutdown and restart using the same DatabaseConfig
    db.shutdown(true, false);

    Database restartedServer = DatabaseFactory.create(config);
    restartedServer.shutdown(true, false);
  }

  public static class OnStartup implements ServerConfigStartup {

    DatabaseConfig calledWithConfig;

    @Override
    public void onStart(DatabaseConfig serverConfig) {
      calledWithConfig = serverConfig;
    }
  }


  public static class OnStartupViaClass implements ServerConfigStartup {

    static DatabaseConfig calledWithConfig;

    @Override
    public void onStart(DatabaseConfig serverConfig) {
      calledWithConfig = serverConfig;
    }
  }
}
