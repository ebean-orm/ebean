package io.ebeaninternal.server.rawsql;

import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.SqlRow;
import io.ebean.service.SpiRawSqlService;
import io.ebeaninternal.server.query.DefaultSqlRow;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

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

      if (ret.containsKey(name)) {
        name = combine(meta.getSchemaName(i), meta.getTableName(i), name);
      }
      ret.put(name, resultSet.getObject(i));

      // convert (C/B)LOBs to java objects.
      // A java.sql.Clob depends on an open connection, so storing this object in a map
      // that is accessed later, when the connection is closed, will result in a "connection is closed" exception.
      switch (meta.getColumnType(i)) {
      case Types.CLOB:
      case Types.NCLOB:
      ret.put(name, resultSet.getString(i));
      break;

      case Types.BLOB:
        ret.put(name, resultSet.getBytes(i));
        break;

      default:
        ret.put(name, resultSet.getObject(i));
        break;
      }

    }
    return ret;
  }

  /**
   * Combine schema table and column names allowing for null schema and table.
   */
  String combine(String schemaName, String tableName, String name) {

    StringBuilder sb = new StringBuilder();
    if (schemaName != null) {
      sb.append(schemaName).append(".");
    }
    if (tableName != null) {
      sb.append(tableName).append(".");
    }
    return sb.append(name).toString();
  }
}
