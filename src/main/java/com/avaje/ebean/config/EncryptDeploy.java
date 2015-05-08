package com.avaje.ebean.config;

/**
 * Define the encryption options for a bean property.
 * <p>
 * You can define the encryption options for a Bean property via the Encrypt
 * annotation and programmatically via {@link EncryptDeployManager}.
 * </p>
 * 
 * @author rbygrave
 * 
 * @see EncryptDeployManager#getEncryptDeploy(TableName, String)
 */
public class EncryptDeploy {

  /**
   * Use to define that no encryption should be used.
   */
  public static final EncryptDeploy NO_ENCRYPT = new EncryptDeploy(Mode.MODE_NO_ENCRYPT, true, 0);

  /**
   * Use to define that the Encrypt annotation should be used to control
   * encryption.
   */
  public static final EncryptDeploy ANNOTATION = new EncryptDeploy(Mode.MODE_ANNOTATION, true, 0);

  /**
   * Use to define that Encryption should be used and String types should use DB
   * encryption.
   */
  public static final EncryptDeploy ENCRYPT_DB = new EncryptDeploy(Mode.MODE_ENCRYPT, true, 0);

  /**
   * Use to define that Java client Encryption should be used (rather than DB
   * encryption).
   */
  public static final EncryptDeploy ENCRYPT_CLIENT = new EncryptDeploy(Mode.MODE_ENCRYPT, false, 0);

  /**
   * The Encryption mode.
   */
  public enum Mode {
    /**
     * Encrypt the property using DB encryption or Java client encryption
     * depending on the type and dbEncryption flag.
     */
    MODE_ENCRYPT,

    /**
     * No encryption is used, even if there is an Encryption annotation on the
     * property.
     */
    MODE_NO_ENCRYPT,

    /**
     * Use encryption options defined by the Encryption annotation on the
     * property. If no annotation is on the property it is not encrypted.
     */
    MODE_ANNOTATION
  }

  private final Mode mode;

  private final boolean dbEncrypt;

  private final int dbLength;

  /**
   * Construct with all options for Encryption including the dbLength.
   * 
   * @param mode
   *          the Encryption mode
   * @param dbEncrypt
   *          set to false if you want to use Java client side encryption rather
   *          than DB encryption.
   * @param dbLength
   *          set the DB length to use.
   */
  public EncryptDeploy(Mode mode, boolean dbEncrypt, int dbLength) {
    this.mode = mode;
    this.dbEncrypt = dbEncrypt;
    this.dbLength = dbLength;
  }

  /**
   * Return the encryption mode.
   */
  public Mode getMode() {
    return mode;
  }

  /**
   * Return true if String type should use DB encryption.
   * <p>
   * Return false if String type should use java client encryption instead.
   * </p>
   */
  public boolean isDbEncrypt() {
    return dbEncrypt;
  }

  /**
   * Return a hint to specify the DB length.
   * <p>
   * Returning 0 means just use the normal DB length determination.
   * </p>
   */
  public int getDbLength() {
    return dbLength;
  }
}
