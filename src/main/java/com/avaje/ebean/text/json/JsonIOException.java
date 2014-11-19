package com.avaje.ebean.text.json;

/**
 * Unchecked exception thrown when an IOException occurs in json processing.
 * <p>
 * Typically wraps the checked IOException.
 * </p>
 */
public class JsonIOException extends RuntimeException {

  /**
   * Construct with an underlying cause.
   */
  public JsonIOException(Throwable cause) {
    super(cause);
  }

  /**
   * Construct with a message.
   */
  public JsonIOException(String message) {
    super(message);
  }
}
