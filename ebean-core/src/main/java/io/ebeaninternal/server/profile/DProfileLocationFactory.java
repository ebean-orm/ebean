package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetric;
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
  public ProfileLocation create(int lineNumber, String label) {

    TimedMetric timedMetric = MetricFactory.get().createTimedMetric("txn.named." + label);

    DTimedProfileLocation loc = new DTimedProfileLocation(lineNumber, label, timedMetric);
    TimedProfileLocationRegistry.register(loc);
    return loc;
  }

  @Override
  public ProfileLocation createAt(String location) {
    return new BasicProfileLocation(location);
  }
}
