package io.ebean.text.json;

/**
 * Unchecked exception thrown when an IOException occurs in json processing.
 * <p>
 * Typically wraps the checked IOException.
 * </p>
 */
public class JsonIOException extends RuntimeException {

  private static final long serialVersionUID = 3062982368161342209L;

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
