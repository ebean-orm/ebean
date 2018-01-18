package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;
import io.ebean.service.SpiProfileLocationFactory;

/**
 * Default implementation of the profile location factory.
 */
public class DProfileLocationFactory implements SpiProfileLocationFactory {

  @Override
  public ProfileLocation create() {
    return new DProfileLocation();
  }

  @Override
  public ProfileLocation create(int lineNumber) {
    return new DProfileLocation(lineNumber);
  }

  @Override
  public ProfileLocation create(String location) {
    return new BasicProfileLocation(location);
  }
}
