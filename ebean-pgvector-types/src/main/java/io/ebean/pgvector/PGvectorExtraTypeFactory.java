package io.ebean.pgvector;

import io.ebean.DatabaseBuilder;
import io.ebean.core.type.ExtraTypeFactory;
import io.ebean.core.type.ScalarType;

import java.util.List;

public final class PGvectorExtraTypeFactory implements ExtraTypeFactory {

  @Override
  public List<? extends ScalarType<?>> createTypes(DatabaseBuilder.Settings config, Object objectMapper) {
    return List.of(
      new ScalarTypePGvector(),
      new ScalarTypePGhalfvec(),
      new ScalarTypePGsparsevec(),
      new ScalarTypePGbit()
    );
  }
}
