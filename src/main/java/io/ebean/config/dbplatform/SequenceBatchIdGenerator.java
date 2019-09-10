package io.ebean.config.dbplatform;

import io.ebean.BackgroundExecutor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database sequence based IdGenerator using Sequence Step 1 but batch fetch many sequence values.
 */
public abstract class SequenceBatchIdGenerator extends SequenceIdGenerator {

  /**
   * Construct where batchSize is the sequence step size.
   *
   */
  public SequenceBatchIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {
    super(be, ds, seqName, batchSize);
  }

  /**
   * If allocateSize is large load some sequences in a background thread.
   * <p>
   * For example, when inserting a bean with a cascade on a OneToMany with many
   * beans Ebean can call this to ensure .
   * </p>
   */
  @Override
  public void preAllocateIds(int requestSize) {
    if (allocationSize > 1 && requestSize > allocationSize) {
      // only bother if allocateSize is bigger than
      // the normal loading batchSize
      if (requestSize > 100) {
        // max out at 100 for now
        requestSize = 100;
      }
      loadInBackground(requestSize);
    }
  }

  /**
   * Add the next set of Ids as the next value plus all the following numbers up to the step size.
   */
  @Override
  protected List<Long> readIds(ResultSet resultSet, int loadSize) throws SQLException {

    List<Long> newIds = new ArrayList<>(loadSize);
    while (resultSet.next()) {
      newIds.add(resultSet.getLong(1));
    }
    return newIds;
  }

}
