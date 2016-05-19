package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.RawSql.ColumnMapping;
import com.avaje.ebean.config.dbplatform.SqlLimitResponse;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.type.DataReader;
import com.avaje.ebeaninternal.server.type.RsetDataReaderIndexed;

import java.sql.ResultSet;
import java.util.List;

/**
 * RawSql based query plan.
 */
public class CQueryPlanRawSql extends CQueryPlan {

  private final int[] rsetIndexPositions;

  public CQueryPlanRawSql(OrmQueryRequest<?> request, SqlLimitResponse sqlRes, SqlTree sqlTree, String logWhereSql) {
    super(request, sqlRes, sqlTree, true, logWhereSql);
    this.rsetIndexPositions = createIndexPositions(request, sqlTree);
  }

  @Override
  public DataReader createDataReader(ResultSet rset) {
    return new RsetDataReaderIndexed(dataTimeZone, rset, rsetIndexPositions, isRowNumberIncluded());
  }

  private int[] createIndexPositions(OrmQueryRequest<?> request, SqlTree sqlTree) {

    List<String> chain = sqlTree.buildRawSqlSelectChain();
    ColumnMapping columnMapping = request.getQuery().getRawSql().getColumnMapping();

    int[] indexPositions = new int[chain.size()];

    // set the resultSet index positions for the property expressions
    for (int i = 0; i < chain.size(); i++) {
      indexPositions[i] = 1 + columnMapping.getIndexPosition(chain.get(i));
    }

    // check and handle the case where a discriminator column for
    // an associated bean is in the raw SQL but is mapped columnIgnore
    for (int i = 0; i < indexPositions.length; i++) {
      if (indexPositions[i] == 0) {
        if (i < indexPositions.length) {
          // expect discriminator column to immediately proceed id column
          indexPositions[i] = indexPositions[i + 1] - 1;
        }
      }
    }

    return indexPositions;
  }
}
