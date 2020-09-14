package io.ebeaninternal.server.autotune.service;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.autotune.AutoTuneService;

public class AutoTuneServiceFactory {

  public static AutoTuneService create(SpiEbeanServer server, DatabaseConfig config) {
    return new DefaultAutoTuneService(server, config);
  }

}
