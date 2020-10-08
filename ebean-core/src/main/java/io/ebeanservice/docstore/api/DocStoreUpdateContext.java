package io.ebeanservice.docstore.api;

/**
 * The doc store specific context/transaction used to collect updates to send to the document store.
 * <p>
 * Doc store specific implementations gather changes and bulk update the document store.
 * </p>
 */
public interface DocStoreUpdateContext {

}
