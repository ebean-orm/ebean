package io.ebean.plugin;

import io.ebean.FetchPath;
import io.ebean.Query;
import io.ebean.docstore.DocUpdateContext;

import java.io.IOException;

/**
 * Doc store functions for a specific entity bean type.
 *
 * @param <T> The type of entity bean
 */
public interface BeanDocType<T> {

  /**
   * Return the doc store index type for this bean type.
   */
  default String indexType() {
    return getIndexType();
  }

  /**
   * Deprecated migrate to indexType().
   */
  @Deprecated
  String getIndexType();

  /**
   * Return the doc store index name for this bean type.
   */
  default String indexName() {
    return getIndexName();
  }

  /**
   * Deprecated migrate to indexName().
   */
  @Deprecated
  String getIndexName();

  /**
   * Apply the appropriate fetch path to the query such that the query returns beans matching
   * the document store structure with the expected embedded properties.
   */
  void applyPath(Query<T> spiQuery);

  /**
   * Return the FetchPath for the embedded document.
   */
  default FetchPath embedded(String path) {
    return getEmbedded(path);
  }

  /**
   * Deprecated migrate to embedded().
   */
  @Deprecated
  FetchPath getEmbedded(String path);

  /**
   * For embedded 'many' properties we need a FetchPath relative to the root which is used to
   * build and replace the embedded list.
   */
  default FetchPath embeddedManyRoot(String path) {
    return getEmbeddedManyRoot(path);
  }

  /**
   * Deprecated migrate to embeddedManyRoot().
   */
  @Deprecated
  FetchPath getEmbeddedManyRoot(String path);

  /**
   * Return a 'raw' property mapped for the given property.
   * If none exists the given property is returned.
   */
  String rawProperty(String property);

  /**
   * Store the bean in the doc store index.
   * <p>
   * This somewhat assumes the bean is fetched with appropriate path properties
   * to match the expected document structure.
   */
  void index(Object idValue, T bean, DocUpdateContext txn) throws IOException;

  /**
   * Add a delete by Id to the doc store.
   */
  void deleteById(Object idValue, DocUpdateContext txn) throws IOException;

  /**
   * Add a embedded document update to the doc store.
   *
   * @param idValue            the Id value of the bean holding the embedded document
   * @param embeddedProperty   the embedded property
   * @param embeddedRawContent the content of the embedded document in JSON form
   * @param txn                the doc store transaction to add the update to
   */
  void updateEmbedded(Object idValue, String embeddedProperty, String embeddedRawContent, DocUpdateContext txn) throws IOException;

}
