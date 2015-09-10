package com.avaje.ebeaninternal.server.autotune.service;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.autotune.AutoTuneService;

public class AutoTuneServiceFactory {

  public static AutoTuneService create(SpiEbeanServer server, ServerConfig serverConfig) {

    return new DefaultAutoTuneService(server, serverConfig);
  }

}
