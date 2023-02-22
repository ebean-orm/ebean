package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;
import io.ebean.metric.MetricFactory;
import io.ebean.service.SpiProfileLocationFactory;

/**
 * Default implementation of the profile location factory.
 */
public final class DProfileLocationFactory implements SpiProfileLocationFactory {

  @Override
  public ProfileLocation create() {
    return new DProfileLocation();
  }

  @Override
  public ProfileLocation create(String label) {
    final var timedMetric = MetricFactory.get().createTimedMetric("txn.named." + label);
    final var loc = new DTimedProfileLocation(label, timedMetric);
    TimedProfileLocationRegistry.register(loc);
    return loc;
  }

  @Override
  public ProfileLocation createAt(String location) {
    return new BasicProfileLocation(location);
  }
}
