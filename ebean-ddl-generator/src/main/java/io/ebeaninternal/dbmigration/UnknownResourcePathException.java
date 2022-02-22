package io.ebeaninternal.dbmigration;

/**
 * Exception when db migration resource path does not exist.
 * <p>
 * Typically the working directory or pathToResources is incorrect.
 */
public class UnknownResourcePathException extends RuntimeException {

  private static final long serialVersionUID = 8533769929372106003L;

  public UnknownResourcePathException(String msg) {
    super(msg);
  }
}
