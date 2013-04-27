package com.avaje.ebean;

import org.avaje.agentloader.AgentLoader;

import junit.framework.TestCase;

public class BaseTestCase extends TestCase {
  
  static {
    AgentLoader.loadAgentFromClasspath("avaje-ebeanorm-agent","debug=1");
  }

}
