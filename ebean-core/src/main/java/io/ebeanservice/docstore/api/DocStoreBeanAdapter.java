package io.ebeanservice.docstore.api;

import io.ebean.Query;
import io.ebean.annotation.DocStoreMode;
import io.ebean.plugin.BeanDocType;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeanservice.docstore.api.mapping.DocumentMapping;

import java.io.IOException;
import java.util.Set;

/**
 * Doc store specific adapter to process doc store events for a given bean type.
 */
public interface DocStoreBeanAdapter<T> extends BeanDocType<T> {

  /**
   * In deployment phase read the embedded/nested document information.
   */
  void registerPaths();

  /**
   * Register invalidation events for embedded/nested documents the given path and properties.
   */
  void registerInvalidationPath(String queueId, String path, Set<String> properties);

  /**
   * Apply the document structure to the query so that it fetches the required properties to build
   * the document (typically in JSON form).
   */
  @Override
  void applyPath(Query<T> query);

  /**
   * Return true if this type is mapped for doc storage.
   */
  boolean isMapped();

  /**
   * Return the unique queueId for this bean type. This is expected to be a relatively short unique
   * string (rather than a fully qualified class name).
   */
  String getQueueId();

  /**
   * Determine and return how this persist type will be processed given the transaction mode.
   * <p>
   * Some transactions (like bulk updates) might specifically turn off indexing for example.
   */
  DocStoreMode getMode(PersistRequest.Type persistType, DocStoreMode txnMode);

  /**
   * Return the index type for this bean type.
   */
  @Override
  String getIndexType();

  /**
   * Return the index name for this bean type.
   */
  @Override
  String getIndexName();

  /**
   * Process a delete by id of a given document.
   */
  @Override
  void deleteById(Object idValue, DocStoreUpdateContext txn) throws IOException;

  /**
   * Process an index event which is effectively an insert or update (or put).
   */
  @Override
  void index(Object idValue, T entityBean, DocStoreUpdateContext txn) throws IOException;

  /**
   * Process an insert persist request.
   */
  void insert(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext txn) throws IOException;

  /**
   * Process an update persist request.
   */
  void update(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext txn) throws IOException;

  /**
   * Process the persist request adding any embedded/nested document invalidation to the docStoreUpdates.
   * <p>
   * This is expected to check the specific properties to see what other documents they are nested in
   * and register invalidation events based on that.
   *
   * @param request         The persist request
   * @param docStoreUpdates Invalidation events are registered to this docStoreUpdates
   */
  void updateEmbedded(PersistRequestBean<T> request, DocStoreUpdates docStoreUpdates);

  /**
   * Process an update of an embedded document.
   *
   * @param idValue            the id of the bean effected by an embedded document update
   * @param embeddedProperty   the path of the property
   * @param embeddedRawContent the embedded content for this property in JSON form
   * @param txn                the doc store transaction to use to process the update
   */
  @Override
  void updateEmbedded(Object idValue, String embeddedProperty, String embeddedRawContent, DocStoreUpdateContext txn) throws IOException;

  /**
   * Create the document mapping.
   */
  DocumentMapping createDocMapping();

  /**
   * Return an un-analysed property to use instead of the given property.
   * <p>
   * For analysed properties that we want to sort on we will map the property to an additional
   * 'raw' property that we can use for sorting etc.
   * </p>
   */
  @Override
  String rawProperty(String property);

  /**
   * Return true if this bean type as embedded invalidate registered.
   */
  boolean hasEmbeddedInvalidation();
}
