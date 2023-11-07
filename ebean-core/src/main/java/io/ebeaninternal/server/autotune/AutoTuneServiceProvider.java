package io.ebeaninternal.server.autotune;

import io.ebean.DatabaseBuilder;
import io.ebeaninternal.api.SpiEbeanServer;

public interface AutoTuneServiceProvider {

  AutoTuneService create(SpiEbeanServer server, DatabaseBuilder config);
}
