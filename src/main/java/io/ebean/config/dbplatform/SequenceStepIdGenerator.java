package io.ebean.config.dbplatform;

import io.ebean.BackgroundExecutor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database sequence based IdGenerator using Sequence Step (e.g. step 50).
 */
public abstract class SequenceStepIdGenerator extends SequenceIdGenerator {

  private final String nextSql;
  /**
   * Construct with stepSize (typically 50).
   */
  protected SequenceStepIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int stepSize, String nextSql) {
    super(be, ds, seqName, stepSize);
    this.nextSql = nextSql;
  }

  @Override
  public String getSql() {
    return nextSql;
  }

  /**
   * Add the next set of Ids as the next value plus all the following numbers up to the step size.
   */
  @Override
  protected List<Long> readIds(ResultSet resultSet) throws SQLException {
    List<Long> newIds = new ArrayList<>(allocationSize);
    if (resultSet.next()) {
      long start = resultSet.getLong(1);
      for (int i = 0; i < allocationSize; i++) {
        newIds.add(start + i);
      }
    }
    return newIds;
  }

}
