package io.ebean.joda.time;

import io.ebean.config.DatabaseConfig;
import io.ebean.core.type.ExtraTypeFactory;
import io.ebean.core.type.ScalarType;

import java.util.ArrayList;
import java.util.List;

/**
 * Register the Joda-Time ScalarType support.
 */
public class JodaExtraTypeFactory implements ExtraTypeFactory {

  @Override
  public List<? extends ScalarType<?>> createTypes(DatabaseConfig config, Object objectMapper) {
    var jsonDateTime = config.getJsonDateTime();
    var jsonDate = config.getJsonDate();

    List<ScalarType<?>> types = new ArrayList<>();
    types.add(new ScalarTypeJodaLocalDateTime(jsonDateTime));
    types.add(new ScalarTypeJodaDateTime(jsonDateTime));
    if (config.getDatabasePlatform().supportsNativeJavaTime()) {
      types.add(new ScalarTypeJodaLocalDateNative(jsonDate));
    } else {
      types.add(new ScalarTypeJodaLocalDate(jsonDate));
    }
    types.add(new ScalarTypeJodaDateMidnight(jsonDate));
    types.add(new ScalarTypeJodaPeriod());
    String jodaLocalTimeMode = config.getJodaLocalTimeMode();
    if ("normal".equalsIgnoreCase(jodaLocalTimeMode)) {
      // use the expected/normal local time zone
      types.add(new ScalarTypeJodaLocalTime());
    } else if ("utc".equalsIgnoreCase(jodaLocalTimeMode)) {
      // use the old UTC based
      types.add(new ScalarTypeJodaLocalTimeUTC());
    }
    return types;
  }
}
