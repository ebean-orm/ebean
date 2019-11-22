package io.ebean.text;

/**
 * An exception occurred typically in processing CSV, JSON or XML.
 */
public class TextException extends RuntimeException {

  private static final long serialVersionUID = 1601310159486033148L;
  private String text;

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

  /**
   * Constructor for a detailed exception.
   *
   * @param message
   *          the message. The placeholder {} will be replaced by
   *          <code>text</code>
   * @param text
   *          the fault text.
   * @param cause
   *          the case
   */
  public TextException(String message, String text, Exception cause) {
    super(message.replace("{}", String.valueOf(text)), cause);
    this.text = text;
  }

  /**
   * Constructor for a detailed exception.
   *
   * @param message
   *          the message. The placeholder {} will be replaced by
   *          <code>text</code>
   * @param text
   *          the fault text.
   */
  public TextException(String message, String text) {
    super(message.replace("{}", String.valueOf(text)));
    this.text = text;
  }

  /**
   * Return the text, that caused the error. (e.g. the JSON). May be null.
   */
  public String getText() {
    return text;
  }
}
