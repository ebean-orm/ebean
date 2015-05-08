package com.avaje.ebean.text;

/**
 * An exception occurred typically in processing CSV, JSON or XML.
 * 
 * @author rbygrave
 */
public class TextException extends RuntimeException {

  private static final long serialVersionUID = 1601310159486033148L;

  /**
   * Construct with an error message.
   */
  public TextException(String msg) {
    super(msg);
  }

  /**
   * Construct with a message and cause.
   */
  public TextException(String msg, Exception e) {
    super(msg, e);
  }

  /**
   * Construct with a cause.
   */
  public TextException(Exception e) {
    super(e);
  }
}
