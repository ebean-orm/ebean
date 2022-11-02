import io.ebeaninternal.server.autotune.AutoTuneServiceProvider;
import io.ebeaninternal.server.autotune.service.AutoTuneServiceFactory;

/**
 * Provider of AutoTuneServiceProvider
 */
module io.ebean.autotune {

  provides AutoTuneServiceProvider with AutoTuneServiceFactory;

  requires io.ebean.api;
  requires io.ebean.core;
  requires java.xml;
  requires java.xml.bind;
}
