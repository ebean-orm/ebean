package com.avaje.ebean.config.dbplatform;

/**
 * H2 encryption support via encrypt decrypt function.
 * 
 * @author rbygrave
 */
public class H2DbEncrypt extends AbstractDbEncrypt {

  public H2DbEncrypt() {
    this.varcharEncryptFunction = new H2VarcharFunction();
    this.dateEncryptFunction = new H2DateFunction();
  }

  /**
   * For H2 encrypt function returns false binding the key before the data.
   */
  public boolean isBindEncryptDataFirst() {
    return false;
  }

  private static class H2VarcharFunction implements DbEncryptFunction {

    public String getDecryptSql(String columnWithTableAlias) {
      // Hmmm, this looks ugly - checking with H2 Database folks.
      return "TRIM(CHAR(0) FROM UTF8TOSTRING(DECRYPT('AES', STRINGTOUTF8(?), "
          + columnWithTableAlias + ")))";
    }

    public String getEncryptBindSql() {
      return "ENCRYPT('AES', STRINGTOUTF8(?), STRINGTOUTF8(?))";
    }

  }

  private static class H2DateFunction implements DbEncryptFunction {

    public String getDecryptSql(String columnWithTableAlias) {
      return "PARSEDATETIME(TRIM(CHAR(0) FROM UTF8TOSTRING(DECRYPT('AES', STRINGTOUTF8(?), "
          + columnWithTableAlias + "))),'yyyyMMdd')";
    }

    public String getEncryptBindSql() {
      return "ENCRYPT('AES', STRINGTOUTF8(?), STRINGTOUTF8(FORMATDATETIME(?,'yyyyMMdd')))";
    }

  }
}
