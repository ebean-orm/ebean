package io.ebeaninternal.server.query;

import io.ebeaninternal.server.rawsql.SpiRawSql.ColumnMapping;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.type.DataReader;
import io.ebeaninternal.server.type.RsetDataReaderIndexed;

import java.sql.ResultSet;
import java.util.List;

/**
 * RawSql based query plan.
 */
class CQueryPlanRawSql extends CQueryPlan {

  private final int[] rsetIndexPositions;

  CQueryPlanRawSql(OrmQueryRequest<?> request, SqlLimitResponse sqlRes, SqlTree sqlTree, String logWhereSql) {
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
      String logicalPropertyPath = chain.get(i);
      int mappedPosition = columnMapping.getIndexPosition(logicalPropertyPath);
      if (mappedPosition == -1 && logicalPropertyPath.endsWith(".id")) {
        // try a automatically mapped foreign key
        mappedPosition = columnMapping.getIndexPosition(foreignKeyPath(logicalPropertyPath));
      }
      indexPositions[i] = 1 + mappedPosition;
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

  /**
   * Return the path for a foreign key column that was automatically mapped.
   */
  private String foreignKeyPath(String logicalPropertyPath) {
    // trim the .id and replace with Id ... to reverse the auto fk mapping earlier
    return logicalPropertyPath.substring(0, logicalPropertyPath.length() - 3) + "Id";
  }
}
