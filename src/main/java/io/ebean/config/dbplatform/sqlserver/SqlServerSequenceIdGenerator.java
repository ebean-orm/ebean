package io.ebean.config.dbplatform.sqlserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.ebean.BackgroundExecutor;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.dbplatform.SequenceIdGenerator;

/**
 * SQLServer specific sequence Id Generator.
 */
public class SqlServerSequenceIdGenerator extends SequenceIdGenerator {

  private final String sql1;
  private final String sql2;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public SqlServerSequenceIdGenerator(BackgroundExecutor be, TenantDataSourceProvider ds, String seqName, int batchSize, CurrentTenantProvider currentTenantProvider) {
    super(be, ds, seqName, batchSize, currentTenantProvider);
    this.sql1 = "DECLARE @res sql_variant; EXEC sp_sequence_get_range @sequence_name = N'" + seqName +"', @range_size = ";
    this.sql2 = ", @range_first_value = @res OUTPUT; SELECT CAST(@res AS bigint)"  ;
  }

  @Override
  public String getSql(int batchSize) {
    StringBuilder sb = new StringBuilder(sql1).append(batchSize).append(sql2);
    return sb.toString();
  }
  
  @Override
  protected List<Long> processResult(ResultSet rset, int loadSize) throws SQLException {
    List<Long> newIds = new ArrayList<>(loadSize);
    if (rset.next()) {
      long start = rset.getLong(1);
      for (int i = 0; i < loadSize; i++) {
        newIds.add(start++);
      }
   }
   return newIds;
  }
}
