package com.avaje.ebean.config.dbplatform;

/**
 * Postgres pgp_sym_encrypt pgp_sym_decrypt based encryption support.
 * 
 * @author rbygrave
 */
public class PostgresDbEncrypt extends AbstractDbEncrypt {

  public PostgresDbEncrypt() {
    this.varcharEncryptFunction = new PgVarcharFunction();
    this.dateEncryptFunction = new PgDateFunction();
  }

  private static class PgVarcharFunction implements DbEncryptFunction {

    public String getDecryptSql(String columnWithTableAlias) {
      return "pgp_sym_decrypt(" + columnWithTableAlias + ",?)";
    }

    public String getEncryptBindSql() {
      return "pgp_sym_encrypt(?,?)";
    }
  }

  private static class PgDateFunction implements DbEncryptFunction {

    public String getDecryptSql(String columnWithTableAlias) {
      return "to_date(pgp_sym_decrypt(" + columnWithTableAlias + ",?),'YYYYMMDD')";
    }

    public String getEncryptBindSql() {
      return "pgp_sym_encrypt(to_char(?::date,'YYYYMMDD'),?)";
    }
  }
}
