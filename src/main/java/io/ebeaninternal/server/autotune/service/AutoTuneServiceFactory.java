package io.ebeaninternal.server.autotune.service;

import io.ebean.config.ServerConfig;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.autotune.AutoTuneService;

public class AutoTuneServiceFactory {

  public static AutoTuneService create(SpiEbeanServer server, ServerConfig serverConfig) {

    return new DefaultAutoTuneService(server, serverConfig);
  }

}
