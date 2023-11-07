package org.example;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.annotation.PersistBatch;
import io.ebean.DatabaseBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A Spring FactoryBean for constructing EbeanServer instances.
 *
 * @since 18.05.2009
 * @author E Mc Greal
 */
public class EbeanServerFactoryBean implements InitializingBean, FactoryBean<Database> {

  /**
   * The Ebean server configuration.
   */
  private DatabaseBuilder serverConfig;

  /**
   * The EbeanServer instance.
   */
  private Database ebeanServer;

  public void afterPropertiesSet() throws Exception {

    if (serverConfig == null) {
      throw new Exception("No ServerConig set. You must define a ServerConfig bean");
    }

    serverConfig.setPersistBatch(PersistBatch.ALL);
    // Create the new EbeanServer using the configuration
    this.ebeanServer = DatabaseFactory.create(serverConfig);
  }

  public Database getObject() throws Exception {
    return ebeanServer;
  }

  public Class<? extends Database> getObjectType() {
    return Database.class;
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
  public DatabaseBuilder getServerConfig() {
    return serverConfig;
  }

  /**
   * Set the server configuration.
   */
  public void setServerConfig(DatabaseBuilder serverConfig) {
    this.serverConfig = serverConfig;
  }
}
