package io.ebeaninternal.server.rawsql;

import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.SqlRow;
import io.ebean.service.SpiRawSqlService;
import io.ebeaninternal.server.query.DefaultSqlRow;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DRawSqlService implements SpiRawSqlService {

  @Override
  public RawSql resultSet(ResultSet resultSet, String... propertyNames) {
    return new DRawSql(resultSet, propertyNames);
  }

  @Override
  public RawSqlBuilder parsed(String sql) {

    SpiRawSql.Sql sql2 = DRawSqlParser.parse(sql);
    String select = sql2.getPreFrom();

    SpiRawSql.ColumnMapping mapping = DRawSqlColumnsParser.parse(select);
    return new DRawSqlBuilder(sql2, mapping);
  }

  @Override
  public RawSqlBuilder unparsed(String sql) {
    SpiRawSql.Sql s = new SpiRawSql.Sql(sql);
    return new DRawSqlBuilder(s, new SpiRawSql.ColumnMapping());
  }

  @Override
  public SqlRow sqlRow(ResultSet resultSet, String dbTrueValue, boolean binaryOptimizedUUID) throws SQLException {

    ResultSetMetaData meta = resultSet.getMetaData();
    int estCap = (int) (meta.getColumnCount() / 0.7f) + 1;
    DefaultSqlRow ret = new DefaultSqlRow(estCap, 0.75f, dbTrueValue, binaryOptimizedUUID);

    for (int i = 1; i <= meta.getColumnCount(); i++) {
      String name = meta.getColumnLabel(i);
      if (name == null) {
        name = meta.getColumnName(i);
      }
      ret.put(name, resultSet.getObject(i));
    }
    return ret;
  }
}
