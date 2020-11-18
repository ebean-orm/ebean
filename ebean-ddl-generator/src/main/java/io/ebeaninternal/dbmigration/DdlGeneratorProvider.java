package io.ebeaninternal.dbmigration;

import io.ebeaninternal.api.SpiDdlGenerator;
import io.ebeaninternal.api.SpiDdlGeneratorProvider;
import io.ebeaninternal.api.SpiEbeanServer;

public class DdlGeneratorProvider implements SpiDdlGeneratorProvider {

  @Override
  public SpiDdlGenerator generator(SpiEbeanServer server) {
    return new DdlGenerator(server);
  }
}
