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

  public DataReader createDataReader(ResultSet rset) {

    return new RsetDataReaderIndexed(rset, rsetIndexPositions, isRowNumberIncluded());
  }

  private int[] createIndexPositions(OrmQueryRequest<?> request, SqlTree sqlTree) {

    List<String> chain = sqlTree.buildSelectExpressionChain();
    ColumnMapping columnMapping = request.getQuery().getRawSql().getColumnMapping();

    InheritInfo inheritInfo = request.getBeanDescriptor().getInheritInfo();
    boolean addDiscriminator = inheritInfo != null;
    int offset = addDiscriminator ? 1 : 0;

    int[] indexPositions = new int[chain.size() + offset];
    if (addDiscriminator) {
      // discriminator column must always be first in the query
      indexPositions[0] = 1;
    }
    for (int i = 0; i < chain.size(); i++) {
      String expr = chain.get(i);
      int indexPos = 1 + columnMapping.getIndexPosition(expr);
      indexPositions[i + offset] = indexPos;
    }

    return indexPositions;
  }
}
