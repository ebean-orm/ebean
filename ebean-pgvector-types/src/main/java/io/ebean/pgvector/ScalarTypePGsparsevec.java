package io.ebean.pgvector;

import com.pgvector.PGsparsevec;
import io.ebean.config.dbplatform.ExtraDbTypes;

import java.sql.SQLException;

public class ScalarTypePGsparsevec extends ScalarTypePGbase<PGsparsevec> {
  public ScalarTypePGsparsevec() {
    super(ExtraDbTypes.VECTOR_SPARSE, PGsparsevec.class);
  }

  @Override
  public PGsparsevec parse(String value) {
    try {
      return new PGsparsevec(value);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
}
