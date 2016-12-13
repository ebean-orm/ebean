/**
 * Copyright (C) 2009 the original author or authors
 * <p>
 * This file is part of Ebean.
 * <p>
 * Ebean is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * <p>
 * Ebean is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
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
