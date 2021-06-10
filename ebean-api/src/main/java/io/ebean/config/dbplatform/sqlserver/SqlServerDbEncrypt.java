package io.ebean.config.dbplatform.sqlserver;

import io.ebean.config.dbplatform.AbstractDbEncrypt;
import io.ebean.config.dbplatform.DbEncryptFunction;

/**
 * SQL Server EncryptByPassPhrase DecryptByPassPhrase based encryption support.
 */
public class SqlServerDbEncrypt extends AbstractDbEncrypt {

  public SqlServerDbEncrypt() {
    this.varcharEncryptFunction = new VarcharFunction();
    this.dateEncryptFunction = new DateFunction();
  }

  @Override
  public boolean isBindEncryptDataFirst() {
    return false;
  }

  private static class VarcharFunction implements DbEncryptFunction {

    @Override
    public String getDecryptSql(String columnWithTableAlias) {
      return "convert(nvarchar,DecryptByPassPhrase(?," + columnWithTableAlias + "))";
    }

    @Override
    public String getEncryptBindSql() {
      return "EncryptByPassPhrase(?,?)";
    }
  }

  private static class DateFunction implements DbEncryptFunction {

    @Override
    public String getDecryptSql(String columnWithTableAlias) {
      return "cast(convert(nvarchar,DecryptByPassPhrase(?," + columnWithTableAlias + ")) as date)";
    }

    @Override
    public String getEncryptBindSql() {
      return "EncryptByPassPhrase(?,format(?,'yyyy-MM-dd'))";
    }
  }
}
