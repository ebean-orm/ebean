package io.ebean.pgvector;

import com.pgvector.PGhalfvec;
import io.ebean.config.dbplatform.ExtraDbTypes;

import java.sql.SQLException;

public final class ScalarTypePGhalfvec extends ScalarTypePGbase<PGhalfvec> {

  public ScalarTypePGhalfvec() {
    super(ExtraDbTypes.VECTOR_HALF, PGhalfvec.class);
  }

  @Override
  public PGhalfvec parse(String value) {
    try {
      return new PGhalfvec(value);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
}
