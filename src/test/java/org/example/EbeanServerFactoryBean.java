package org.example;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;

/**
 * A Spring FactoryBean for constructing EbeanServer instances.
 *
 * @since 18.05.2009
 * @author E Mc Greal
 */
public class EbeanServerFactoryBean implements InitializingBean, FactoryBean<EbeanServer> {

  /**
   * The Ebean server configuration.
   */
  private ServerConfig serverConfig;

  /**
   * The EbeanServer instance.
   */
  private EbeanServer ebeanServer;

  public void afterPropertiesSet() throws Exception {

    if (serverConfig == null) {
      throw new Exception("No ServerConig set. You must define a ServerConfig bean");
    }

    // Create the new EbeanServer using the configuration
    this.ebeanServer = EbeanServerFactory.create(serverConfig);
  }

  public EbeanServer getObject() throws Exception {
    return ebeanServer;
  }

  public Class<? extends EbeanServer> getObjectType() {
    return EbeanServer.class;
  }

  /**
   * Returns true for EbeanServer.
   */
  public boolean isSingleton() {
    return true;
  }

  /**
   * Return the server configuration.
   */
  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  /**
   * Set the server configuration.
   */
  public void setServerConfig(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
  }
}
