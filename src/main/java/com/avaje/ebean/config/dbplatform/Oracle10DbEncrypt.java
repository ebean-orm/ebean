package com.avaje.ebean.config.dbplatform;

/**
 * Oracle encryption support.
 * 
 * <p>
 * You will typically need to create your own encryption and decryption
 * functions similar to the example ones below.
 * </p>
 * 
 * <pre class="code">
 * 
 *  // Remember your DB user needs execute privilege on DBMS_CRYPTO 
 *  // as well as your encryption and decryption functions
 *  
 *  
 *  // This is an Example Encryption function only - please create your own.
 * 
 * CREATE OR REPLACE FUNCTION eb_encrypt(data IN VARCHAR, key in VARCHAR) RETURN RAW IS
 * 
 *     encryption_mode NUMBER := DBMS_CRYPTO.ENCRYPT_AES128 + DBMS_CRYPTO.CHAIN_CBC  + DBMS_CRYPTO.PAD_PKCS5;
 * 
 *     BEGIN
 *          RETURN DBMS_CRYPTO.ENCRYPT(UTL_I18N.STRING_TO_RAW (data, 'AL32UTF8'), 
 *            encryption_mode, UTL_I18N.STRING_TO_RAW(key, 'AL32UTF8') );
 *     END;
 *     /
 *     
 *     
 *     
 *  // This is an Example Decryption function only - please create your own.
 *     
 * CREATE OR REPLACE FUNCTION eb_decrypt(data IN RAW, key IN VARCHAR) RETURN VARCHAR IS
 * 
 *     encryption_mode NUMBER := DBMS_CRYPTO.ENCRYPT_AES128 + DBMS_CRYPTO.CHAIN_CBC  + DBMS_CRYPTO.PAD_PKCS5;
 * 
 *     BEGIN
 *          RETURN UTL_RAW.CAST_TO_VARCHAR2(DBMS_CRYPTO.DECRYPT
 *            (data, encryption_mode, UTL_I18N.STRING_TO_RAW(key, 'AL32UTF8')));
 *     END;
 *     /
 * </pre>
 * 
 * @author rbygrave
 */
public class Oracle10DbEncrypt extends AbstractDbEncrypt {

  /**
   * Constructs the Oracle10DbEncrypt with default encrypt and decrypt stored procedures.
   */
  public Oracle10DbEncrypt() {
    this("eb_encrypt", "eb_decrypt");
  }

  /**
   * Constructs the Oracle10DbEncrypt specifying encrypt and decrypt stored procedures.
   *
   * @param encryptfunction the encrypt stored procedure
   * @param decryptfunction the decrypt stored procedure
   */
  public Oracle10DbEncrypt(String encryptfunction, String decryptfunction) {

    this.varcharEncryptFunction = new OraVarcharFunction(encryptfunction, decryptfunction);
    this.dateEncryptFunction = new OraDateFunction(encryptfunction, decryptfunction);
  }

  /**
   * VARCHAR encryption/decryption function.
   */
  private static class OraVarcharFunction implements DbEncryptFunction {

    private final String encryptfunction;
    private final String decryptfunction;

    public OraVarcharFunction(String encryptfunction, String decryptfunction) {
      this.encryptfunction = encryptfunction;
      this.decryptfunction = decryptfunction;
    }

    public String getDecryptSql(String columnWithTableAlias) {
      return decryptfunction + "(" + columnWithTableAlias + ",?)";
    }

    public String getEncryptBindSql() {
      return encryptfunction + "(?,?)";
    }

  }

  /**
   * DATE encryption/decryption function.
   */
  private static class OraDateFunction implements DbEncryptFunction {

    private final String encryptfunction;
    private final String decryptfunction;

    public OraDateFunction(String encryptfunction, String decryptfunction) {
      this.encryptfunction = encryptfunction;
      this.decryptfunction = decryptfunction;
    }

    public String getDecryptSql(String columnWithTableAlias) {
      return "to_date(" + decryptfunction + "(" + columnWithTableAlias + ",?),'YYYYMMDD')";
    }

    public String getEncryptBindSql() {
      return encryptfunction + "(to_char(?,'YYYYMMDD'),?)";
    }

  }
}
