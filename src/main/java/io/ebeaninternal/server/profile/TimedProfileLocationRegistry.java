package io.ebeaninternal.server.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Global registry of the TimedProfileLocation instances created.
 */
public class TimedProfileLocationRegistry {

  private static final List<TimedProfileLocation> list = Collections.synchronizedList(new ArrayList<TimedProfileLocation>());

  /**
   * Register the timed profile location instance.
   */
  public static void register(TimedProfileLocation location) {
    list.add(location);
  }

  /**
   * Return all the registered timed locations.
   */
  public static List<TimedProfileLocation> registered() {
    return list;
  }
}
