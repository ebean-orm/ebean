package org.tests.unitinternal;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.TOne;

import java.io.File;

public class HelloMain {

  protected static Logger logger = LoggerFactory.getLogger(HelloMain.class);

  public static void main(String[] args) {
    // ### Configuration Objects ###
    DatabaseBuilder serverConfig = new DatabaseConfig();
    DataSourceConfig dataSourceConfig = new DataSourceConfig();

    // ### Configuration Settings ###
    // -> data source
    dataSourceConfig.driver("org.h2.Driver");
    dataSourceConfig.username("howtouser");
    dataSourceConfig.password("");
    dataSourceConfig.url("jdbc:h2:~/db/howto1");

    // -> server
    serverConfig.setName("default");
    serverConfig.setDataSourceConfig(dataSourceConfig);

    // auto create db if it does not exist
    if (!(new File("db/howto1.data.db")).exists()) {
      serverConfig.setDdlGenerate(true);
      serverConfig.setDdlRun(true);
      serverConfig.addClass(TOne.class);
    }

    Database eServer = DatabaseFactory.createWithContextClassLoader(serverConfig, HelloMain.class.getClassLoader());

    long id = 1;
    TOne data = eServer.find(TOne.class, id);
    if (data == null) {
      TOne tone = new TOne();
      tone.setName("banan");
      eServer.save(tone);// new TOne()id, "Hello World!"));
    } else {
      System.out.println(String.format("############\n%s############", data.getName()));
    }

    DatabaseFactory.shutdown();
  }

}
