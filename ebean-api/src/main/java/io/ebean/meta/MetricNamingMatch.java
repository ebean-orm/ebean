package io.ebean.meta;

import java.util.function.Function;

/**
 * Metric naming convention that is exact match.
 */
public final class MetricNamingMatch implements Function<String, String> {

  public static final Function<String, String> INSTANCE = new MetricNamingMatch();

  @Override
  public String apply(String name) {
    return name;
  }
}
