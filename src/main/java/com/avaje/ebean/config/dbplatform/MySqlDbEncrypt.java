package com.avaje.ebean.config.dbplatform;

/**
 * MySql aes_encrypt aes_decrypt based encryption support.
 * 
 * @author rbygrave
 */
public class MySqlDbEncrypt extends AbstractDbEncrypt {

  public MySqlDbEncrypt() {
    this.varcharEncryptFunction = new MyVarcharFunction();
    this.dateEncryptFunction = new MyDateFunction();
  }

  private static class MyVarcharFunction implements DbEncryptFunction {

    public String getDecryptSql(String columnWithTableAlias) {
      return "CONVERT(AES_DECRYPT(" + columnWithTableAlias + ",?) USING UTF8)";
    }

    public String getEncryptBindSql() {
      return "AES_ENCRYPT(?,?)";
    }
  }

  private static class MyDateFunction implements DbEncryptFunction {

    public String getDecryptSql(String columnWithTableAlias) {
      return "STR_TO_DATE(AES_DECRYPT(" + columnWithTableAlias + ",?),'%Y%d%m')";
    }

    public String getEncryptBindSql() {
      return "AES_ENCRYPT(DATE_FORMAT(?,'%Y%d%m'),?)";
    }
  }
}
