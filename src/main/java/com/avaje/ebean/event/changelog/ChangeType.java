package com.avaje.ebean.event.changelog;

/**
 * The type of the change.
 */
public enum ChangeType {

  /**
   * The change was an insert.
   */
  INSERT("I"),

  /**
   * The change was an update.
   */
  UPDATE("U"),

  /**
   * The change was a delete.
   */
  DELETE("D");

  final String code;

  ChangeType(String code) {
    this.code = code;
  }

  /**
   * Return the short code for the ChangeType.
   * <p>
   * I - Insert, U - Update and D - Delete.
   * </p>
   */
  public String getCode() {
    return code;
  }
}
