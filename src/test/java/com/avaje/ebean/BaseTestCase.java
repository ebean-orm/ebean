package com.avaje.ebean;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import org.avaje.agentloader.AgentLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseTestCase {
  
  protected static Logger logger = LoggerFactory.getLogger(BaseTestCase.class);
  
  static {
    logger.debug("... preStart");
    if (!AgentLoader.loadAgentFromClasspath("avaje-ebeanorm-agent","debug=1;packages=com.avaje.tests.**,org.avaje.test.**")) {
      logger.info("avaje-ebeanorm-agent not found in classpath - not dynamically loaded");
    }    
  }

  /**
   * MS SQL Server does not allow setting explicit values on identity columns
   * so tests that do this need to be skipped for SQL Server.
   */
  public boolean isMsSqlServer() {
    SpiEbeanServer spi = (SpiEbeanServer)Ebean.getDefaultServer();
    return spi.getDatabasePlatform().getName().startsWith("mssqlserver");
  }
}
