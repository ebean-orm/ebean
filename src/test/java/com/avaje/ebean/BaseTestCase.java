package com.avaje.ebean;

import org.avaje.agentloader.AgentLoader;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseTestCase {
  
  protected static Logger logger = LoggerFactory.getLogger(BaseTestCase.class);
  
  @BeforeClass
  public static void preStart() {
    logger.debug("... preStart");
    AgentLoader.loadAgentFromClasspath("avaje-ebeanorm-agent","debug=0");
  }

}
