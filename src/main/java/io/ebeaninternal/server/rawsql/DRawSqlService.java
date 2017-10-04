package io.ebeaninternal.server.rawsql;

import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.service.SpiRawSqlService;

import java.sql.ResultSet;

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
}
