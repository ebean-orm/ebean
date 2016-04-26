package com.avaje.ebeaninternal.server.query;

import java.sql.ResultSet;
import java.util.List;

import com.avaje.ebean.RawSql.ColumnMapping;
import com.avaje.ebean.config.dbplatform.SqlLimitResponse;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.type.DataReader;
import com.avaje.ebeaninternal.server.type.RsetDataReaderIndexed;

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

    // if the top level bean has inheritance expect first column
    // to be the discriminator type column (and use offset 1)
    InheritInfo inheritInfo = request.getBeanDescriptor().getInheritInfo();
    boolean addDiscriminator = inheritInfo != null;
    int offset = addDiscriminator ? 1 : 0;

    int[] indexPositions = new int[chain.size() + offset];
    if (addDiscriminator) {
      // discriminator column must always be first in the query
      indexPositions[0] = 1;
    }

    // set the resultSet index positions for the property expressions
    for (int i = 0; i < chain.size(); i++) {
      String expr = chain.get(i);
      int indexPos = 1 + columnMapping.getIndexPosition(expr);
      indexPositions[i + offset] = indexPos;
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
