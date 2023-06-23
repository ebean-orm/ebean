package io.ebean.config;

/**
 * Defines the length-check mode.
 *
 * @author Roland Praml, FOCONIS AG
 */
public enum LengthCheck {
  /**
   * By default, length checking is off. This means, strings/jsons and files are passed to the DB and the DB might or might not check the length.
   * The DB has to check the data length. Note this is not possible for certain datatypes (e.g. clob without size)
   */
  OFF,
  /**
   * When enabling length check, ebean validates strings/json strings and files before saving them to DB.
   */
  ON,
  /**
   * Same as "ON", but take the UTF8-bytelength for validation. This may be useful, if you have an UTF8 based charset (default for DB2)
   */
  UTF8
}
