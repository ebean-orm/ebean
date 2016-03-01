package com.avaje.ebeanservice.docstore.api;

/**
 * Can be thrown when a document is unexpectedly not found in a document store.
 */
public class DocumentNotFoundException extends RuntimeException {

  /**
   * Construct with a message.
   */
  public DocumentNotFoundException(String message) {
    super(message);
  }

}
