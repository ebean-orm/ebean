package io.ebeanservice.docstore.api;

import java.io.IOException;

/**
 * Update the document store using a Ebean ORM query.
 * <p>
 * Executes a forEach query and updates the document store with the bean object graph returned by the query.
 * </p>
 */
public interface DocStoreQueryUpdate<T> {

  /**
   * Process the bean storing in the document store.
   */
  void store(Object idValue, T bean) throws IOException;

  /**
   * Flush the changes to the document store.
   */
  void flush() throws IOException;

}
