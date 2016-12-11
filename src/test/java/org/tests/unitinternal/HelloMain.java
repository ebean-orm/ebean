package org.tests.unitinternal;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import org.tests.model.basic.TOne;
import org.avaje.agentloader.AgentLoader;
import org.avaje.datasource.DataSourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class HelloMain {

  protected static Logger logger = LoggerFactory.getLogger(HelloMain.class);

  static {
    logger.debug("... preStart");
    if (!AgentLoader.loadAgentFromClasspath("avaje-ebeanorm-agent", "debug=1;packages=com.avaje.tests.**")) {
      logger.info("avaje-ebeanorm-agent not found in classpath - not dynamically loaded");
    }
  }

  public static void main(String[] args) {
    // ### Configuration Objects ###
    ServerConfig serverConfig = new ServerConfig();
    DataSourceConfig dataSourceConfig = new DataSourceConfig();

    // ### Configuration Settings ###
    // -> data source
    dataSourceConfig.setDriver("org.h2.Driver");
    dataSourceConfig.setUsername("howtouser");
    dataSourceConfig.setPassword("");
    dataSourceConfig.setUrl("jdbc:h2:~/db/howto1");

    // -> server
    serverConfig.setName("default");
    serverConfig.setDataSourceConfig(dataSourceConfig);

    // auto create db if it does not exist
    if (!(new File("db/howto1.data.db")).exists()) {
      serverConfig.setDdlGenerate(true);
      serverConfig.setDdlRun(true);
      serverConfig.addClass(TOne.class);
    }

    EbeanServer eServer = EbeanServerFactory.createWithContextClassLoader(serverConfig, HelloMain.class.getClassLoader());

    long id = 1;
    TOne data = eServer.find(TOne.class, id);
    if (data == null) {
      TOne tone = new TOne();
      tone.setName("banan");
      eServer.save(tone);// new TOne()id, "Hello World!"));
    } else {
      System.out.println(String.format("############\n%s############", data.getName()));
    }

    EbeanServerFactory.shutdown();
  }

}
