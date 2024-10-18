package io.ebean;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import io.ebean.docstore.DocQueryContext;
import io.ebean.docstore.RawDoc;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Document storage operations.
 */
@NullMarked
public interface DocumentStore {

  /**
   * Update the associated document store using the result of the query.
   * <p>
   * This will execute the query against the database creating a document for each
   * bean graph and sending this to the document store.
   * </p>
   * <p>
   * Note that the select and fetch paths of the query is set for you to match the
   * document structure needed based on <code>@DocStore</code> and <code>@DocStoreEmbedded</code>
   * so what this query requires is the predicates only.
   * </p>
   * <p>
   * This query will be executed using findEach so it is safe to use a query
   * that will fetch a lot of beans. The default bulkBatchSize is used.
   * </p>
   *
   * @param query The query that selects object to send to the document store.
   */
  <T> void indexByQuery(Query<T> query);

  /**
   * Update the associated document store index using the result of the query additionally specifying a
   * bulkBatchSize to use for sending the messages to ElasticSearch.
   *
   * @param query         The query that selects object to send to the document store.
   * @param bulkBatchSize The batch size to use when bulk sending to the document store.
   */
  <T> void indexByQuery(Query<T> query, int bulkBatchSize);

  /**
   * Update the document store for all beans of this type.
   * <p>
   * This is the same as indexByQuery where the query has no predicates and so fetches all rows.
   * </p>
   */
  void indexAll(Class<?> beanType);

  /**
   * Return the bean by fetching it's content from the document store.
   * If the document is not found null is returned.
   * <p>
   * Typically this is called indirectly by findOne() on the query.
   * </p>
   * <pre>{@code
   *
   * Customer customer =
   *   database.find(Customer.class)
   *     .setUseDocStore(true)
   *     .setId(42)
   *     .findOne();
   *
   * }</pre>
   */
  @Nullable
  <T> T find(DocQueryContext<T> request);

  /**
   * Execute the find list query. This request is prepared to execute secondary queries.
   * <p>
   * Typically this is called indirectly by findList() on the query that has setUseDocStore(true).
   * </p>
   * <pre>{@code
   *
   * List<Customer> newCustomers =
   *  database.find(Customer.class)
   *    .setUseDocStore(true)
   *    .where().eq("status, Customer.Status.NEW)
   *    .findList();
   *
   * }</pre>
   */
  <T> List<T> findList(DocQueryContext<T> request);

  /**
   * Execute the query against the document store returning the paged list.
   * <p>
   * The query should have <code>firstRow</code> or <code>maxRows</code> set prior to calling this method.
   * </p>
   * <p>
   * Typically this is called indirectly by findPagedList() on the query that has setUseDocStore(true).
   * </p>
   * <pre>{@code
   *
   * PagedList<Customer> newCustomers =
   *  database.find(Customer.class)
   *    .setUseDocStore(true)
   *    .where().eq("status, Customer.Status.NEW)
   *    .setMaxRows(50)
   *    .findPagedList();
   *
   * }</pre>
   */
  <T> PagedList<T> findPagedList(DocQueryContext<T> request);

  /**
   * Execute the query against the document store with the expectation of a large set of results
   * that are processed in a scrolling resultSet fashion.
   * <p>
   * For example, with the ElasticSearch doc store this uses SCROLL.
   * </p>
   * <p>
   * Typically this is called indirectly by findEach() on the query that has setUseDocStore(true).
   * </p>
   * <pre>{@code
   *
   *  database.find(Order.class)
   *    .setUseDocStore(true)
   *    .where()... // perhaps add predicates
   *    .findEach((Order order) -> {
   *      // process the bean ...
   *    });
   *
   * }</pre>
   */
  <T> void findEach(DocQueryContext<T> query, Consumer<T> consumer);

  /**
   * Execute the query against the document store with the expectation of a large set of results
   * that are processed in a scrolling resultSet fashion.
   * <p>
   * Unlike findEach() this provides the opportunity to stop iterating through the large query.
   * </p>
   * <p>
   * For example, with the ElasticSearch doc store this uses SCROLL.
   * </p>
   * <p>
   * Typically this is called indirectly by findEachWhile() on the query that has setUseDocStore(true).
   * </p>
   * <pre>{@code
   *
   *  database.find(Order.class)
   *    .setUseDocStore(true)
   *    .where()... // perhaps add predicates
   *    .findEachWhile(new Predicate<Order>() {
   *      @Override
   *      public void accept(Order bean) {
   *        // process the bean
   *
   *        // return true to continue, false to stop
   *        // boolean shouldContinue = ...
   *        return shouldContinue;
   *      }
   *    });
   *
   * }</pre>
   */
  <T> void findEachWhile(DocQueryContext<T> query, Predicate<T> consumer);

  /**
   * Find each processing raw documents.
   *
   * @param indexNameType The full index name and type
   * @param rawQuery      The query to execute
   * @param consumer      Consumer to process each document
   */
  void findEach(String indexNameType, String rawQuery, Consumer<RawDoc> consumer);

  /**
   * Find each processing raw documents stopping when the predicate returns false.
   *
   * @param indexNameType The full index name and type
   * @param rawQuery      The query to execute
   * @param consumer      Consumer to process each document until false is returned
   */
  void findEachWhile(String indexNameType, String rawQuery, Predicate<RawDoc> consumer);

  /**
   * Process the queue entries sending updates to the document store or queuing them for later processing.
   */
  long process(List<DocStoreQueueEntry> queueEntries) throws IOException;

  /**
   * Drop the index from the document store (similar to DDL drop table).
   * <pre>{@code
   *
   *   DocumentStore documentStore = database.docStore();
   *
   *   documentStore.dropIndex("product_copy");
   *
   * }</pre>
   */
  void dropIndex(String indexName);

  /**
   * Create an index given a mapping file as a resource in the classPath (similar to DDL create table).
   * <pre>{@code
   *
   *   DocumentStore documentStore = database.docStore();
   *
   *   // uses product_copy.mapping.json resource
   *   // ... to define mappings for the index
   *
   *   documentStore.createIndex("product_copy", null);
   *
   * }</pre>
   *
   * @param indexName the name of the new index
   * @param alias     the alias of the index
   */
  void createIndex(String indexName, String alias);

  /**
   * Modify the settings on an index.
   * <p>
   * For example, this can be used be used to set elasticSearch refresh_interval
   * on an index before a bulk update.
   * </p>
   * <pre>{@code
   *
   *   // refresh_interval -1 ... disable refresh while bulk loading
   *
   *   Map<String,Object> settings = new LinkedHashMap<>();
   *   settings.put("refresh_interval", "-1");
   *
   *   documentStore.indexSettings("product", settings);
   *
   * }</pre>
   * <pre>{@code
   *
   *   // refresh_interval 1s ... restore after bulk loading
   *
   *   Map<String,Object> settings = new LinkedHashMap<>();
   *   settings.put("refresh_interval", "1s");
   *
   *   documentStore.indexSettings("product", settings);
   *
   * }</pre>
   *
   * @param indexName the name of the index to update settings on
   * @param settings  the settings to set on the index
   */
  void indexSettings(String indexName, Map<String, Object> settings);

  /**
   * Copy the index to a new index.
   * <p>
   * This copy process does not use the database but instead will copy from the source index to a destination index.
   * </p>
   * <pre>{@code
   *
   *  long copyCount = documentStore.copyIndex(Product.class, "product_copy");
   *
   * }</pre>
   *
   * @param beanType The bean type of the source index
   * @param newIndex The name of the index to copy to
   * @return the number of documents copied to the new index
   */
  long copyIndex(Class<?> beanType, String newIndex);

  /**
   * Copy entries from an index to a new index but limiting to documents that have been
   * modified since the sinceEpochMillis time.
   * <p>
   * To support this the document needs to have a <code>@WhenModified</code> property.
   * </p>
   * <pre>{@code
   *
   *  long copyCount = documentStore.copyIndex(Product.class, "product_copy", sinceMillis);
   *
   * }</pre>
   *
   * @param beanType The bean type of the source index
   * @param newIndex The name of the index to copy to
   * @return the number of documents copied to the new index
   */
  long copyIndex(Class<?> beanType, String newIndex, long sinceEpochMillis);

  /**
   * Copy from a source index to a new index taking only the documents
   * matching the given query.
   * <pre>{@code
   *
   *  // predicates to select the source documents to copy
   *  Query<Product> query = database.find(Product.class)
   *    .where()
   *      .ge("whenModified", new Timestamp(since))
   *      .ge("name", "A")
   *      .lt("name", "D")
   *      .query();
   *
   *  // copy from the source index to "product_copy" index
   *  long copyCount = documentStore.copyIndex(query, "product_copy", 1000);
   *
   * }</pre>
   *
   * @param query         The query to select the source documents to copy
   * @param newIndex      The target index to copy the documents to
   * @param bulkBatchSize The ElasticSearch bulk batch size, if 0 uses the default.
   * @return The number of documents copied to the new index.
   */
  long copyIndex(Query<?> query, String newIndex, int bulkBatchSize);
}
