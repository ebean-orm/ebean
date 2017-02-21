package io.ebean.config.dbplatform.postgres;

import io.ebean.config.dbplatform.AbstractDbEncrypt;
import io.ebean.config.dbplatform.DbEncryptFunction;

/**
 * Postgres pgp_sym_encrypt pgp_sym_decrypt based encryption support.
 */
public class PostgresDbEncrypt extends AbstractDbEncrypt {

  public PostgresDbEncrypt() {
    this.varcharEncryptFunction = new PgVarcharFunction();
    this.dateEncryptFunction = new PgDateFunction();
  }

  private static class PgVarcharFunction implements DbEncryptFunction {

    @Override
    public String getDecryptSql(String columnWithTableAlias) {
      return "pgp_sym_decrypt(" + columnWithTableAlias + ",?)";
    }

    @Override
    public String getEncryptBindSql() {
      return "pgp_sym_encrypt(?,?)";
    }
  }

  private static class PgDateFunction implements DbEncryptFunction {

    @Override
    public String getDecryptSql(String columnWithTableAlias) {
      return "to_date(pgp_sym_decrypt(" + columnWithTableAlias + ",?),'YYYYMMDD')";
    }

    @Override
    public String getEncryptBindSql() {
      return "pgp_sym_encrypt(to_char(?::date,'YYYYMMDD'),?)";
    }
  }
}
