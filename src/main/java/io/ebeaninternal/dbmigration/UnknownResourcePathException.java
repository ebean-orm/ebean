package io.ebeaninternal.dbmigration;

/**
 * Exception when db migration resource path does not exist.
 * <p>
 * Typically the working directory or pathToResources is incorrect.
 * </p>
 */
public class UnknownResourcePathException extends RuntimeException {

  public UnknownResourcePathException(String msg) {
    super(msg);
  }
}
