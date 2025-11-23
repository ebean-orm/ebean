package io.ebean.pgvector;

import com.pgvector.PGbit;
import io.ebean.config.dbplatform.ExtraDbTypes;

import java.sql.SQLException;

public final class ScalarTypePGbit extends ScalarTypePGbase<PGbit> {

  public ScalarTypePGbit() {
    super(ExtraDbTypes.VECTOR_BIT, PGbit.class);
  }

  @Override
  public PGbit parse(String value) {
    try {
      return new PGbit(value);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
}
