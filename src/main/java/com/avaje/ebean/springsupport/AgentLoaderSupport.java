package com.avaje.ebean.springsupport;

import org.avaje.agentloader.AgentLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * To Setup Enhancement in Spring
 * <bean class="org.ebean.AgentLoaderSupport">
 * <property name="debug" value="1" />
 * <property name="packages" value="org.ebean.**" />
 * </bean>
 * Created by guor on 2015/5/11.
 */
public class AgentLoaderSupport implements InitializingBean {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private int debug;
  private String packages;

  public int getDebug() {
    return debug;
  }

  public void setDebug(int debug) {
    this.debug = debug;
  }

  public String getPackages() {
    return packages;
  }

  public void setPackages(String packages) {
    this.packages = packages;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    String args = "debug=" + getDebug() + ";packages=" + getPackages();
    if (!AgentLoader.loadAgentFromClasspath("avaje-ebeanorm-agent", args)) {
      logger.info("avaje-ebeanorm-agent not found in classpath - not dynamically loaded");
    }
  }
}
