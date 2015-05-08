package com.avaje.ebeaninternal.server.deploy.parse;

import com.avaje.ebean.annotation.Sql;
import com.avaje.ebean.annotation.SqlSelect;
import com.avaje.ebeaninternal.server.deploy.DRawSqlMeta;

/**
 * Read the class level deployment annotations.
 */
public class AnnotationSql extends AnnotationParser {

  public AnnotationSql(DeployBeanInfo<?> info) {
    super(info);
  }

  public void parse() {
    Class<?> cls = descriptor.getBeanType();
    Sql sql = cls.getAnnotation(Sql.class);
    if (sql != null) {
      setSql(sql);
    }

    SqlSelect sqlSelect = cls.getAnnotation(SqlSelect.class);
    if (sqlSelect != null) {
      setSqlSelect(sqlSelect);
    }
  }

  private void setSql(Sql sql) {
    SqlSelect[] select = sql.select();
    for (int i = 0; i < select.length; i++) {
      setSqlSelect(select[i]);
    }
  }

  private void setSqlSelect(SqlSelect sqlSelect) {

    DRawSqlMeta rawSqlMeta = new DRawSqlMeta(sqlSelect);
    descriptor.add(rawSqlMeta);
  }
}
