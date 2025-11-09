package io.ebean.pgvector;

import com.pgvector.PGvector;
import io.ebean.config.dbplatform.ExtraDbTypes;

public final class ScalarTypePGvector extends ScalarTypePGbase<PGvector> {

  public ScalarTypePGvector() {
    super(ExtraDbTypes.VECTOR, PGvector.class);
  }

  @Override
  public PGvector parse(String value) {
    try {
      return new PGvector(value);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
