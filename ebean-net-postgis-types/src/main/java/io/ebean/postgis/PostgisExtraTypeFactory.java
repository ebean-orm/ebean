package io.ebean.postgis;

import io.ebean.DatabaseBuilder;
import io.ebean.core.type.ExtraTypeFactory;
import io.ebean.core.type.ScalarType;

import java.util.Arrays;
import java.util.List;

public class PostgisExtraTypeFactory implements ExtraTypeFactory {

  @Override
  public List<ScalarType<?>> createTypes(DatabaseBuilder.Settings config, Object objectMapper) {
    return Arrays.asList(
      new ScalarTypePgisPoint(),
      new ScalarTypePgisPolygon(),
      new ScalarTypePgisLineString(),
      new ScalarTypePgisMultiPolygon(),
      new ScalarTypePgisMultiPoint(),
      new ScalarTypePgisMultiLineString()
    );
  }
}
