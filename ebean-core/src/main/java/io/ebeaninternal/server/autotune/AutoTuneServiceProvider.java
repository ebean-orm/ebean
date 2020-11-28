package io.ebeaninternal.server.autotune;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.api.SpiEbeanServer;

public interface AutoTuneServiceProvider {

  AutoTuneService create(SpiEbeanServer server, DatabaseConfig config);
}
