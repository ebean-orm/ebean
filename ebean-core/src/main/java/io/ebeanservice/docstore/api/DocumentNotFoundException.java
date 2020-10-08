package io.ebeanservice.docstore.api;

/**
 * Can be thrown when a document is unexpectedly not found in a document store.
 */
public class DocumentNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 2066138180892685276L;

  /**
   * Construct with a message.
   */
  public DocumentNotFoundException(String message) {
    super(message);
  }

}
