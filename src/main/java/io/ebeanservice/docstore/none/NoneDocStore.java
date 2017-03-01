package io.ebeanservice.docstore.none;

import io.ebean.DocStoreQueueEntry;
import io.ebean.DocumentStore;
import io.ebean.PagedList;
import io.ebean.Query;
import io.ebeanservice.docstore.api.DocQueryRequest;
import io.ebeanservice.docstore.api.RawDoc;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * DocumentStore that barfs it is used.
 */
public class NoneDocStore implements DocumentStore {

  public static IllegalStateException implementationNotInClassPath() {
    throw new IllegalStateException("DocStore implementation not included in the classPath. You need to add the maven dependency for avaje-ebeanorm-elastic");
  }

  @Override
  public void indexSettings(String indexName, Map<String, Object> settings) {
    throw implementationNotInClassPath();
  }

  @Override
  public void dropIndex(String newIndex) {
    throw implementationNotInClassPath();
  }

  @Override
  public void createIndex(String indexName, String alias) {
    throw implementationNotInClassPath();
  }

  @Override
  public void indexAll(Class<?> countryClass) {
    throw implementationNotInClassPath();
  }

  @Override
  public long copyIndex(Class<?> beanType, String newIndex) {
    throw implementationNotInClassPath();
  }

  @Override
  public long copyIndex(Class<?> beanType, String newIndex, long epochMillis) {
    throw implementationNotInClassPath();
  }

  @Override
  public long copyIndex(Query<?> query, String newIndex, int bulkBatchSize) {
    throw implementationNotInClassPath();
  }

  @Override
  public <T> void indexByQuery(Query<T> query) {
    throw implementationNotInClassPath();
  }

  @Override
  public <T> void indexByQuery(Query<T> query, int bulkBatchSize) {
    throw implementationNotInClassPath();
  }

  @Override
  public <T> T find(DocQueryRequest<T> request) {
    throw implementationNotInClassPath();
  }

  @Override
  public <T> PagedList<T> findPagedList(DocQueryRequest<T> request) {
    throw implementationNotInClassPath();
  }

  @Override
  public <T> List<T> findList(DocQueryRequest<T> request) {
    throw implementationNotInClassPath();
  }

  @Override
  public <T> void findEach(DocQueryRequest<T> query, Consumer<T> consumer) {
    throw implementationNotInClassPath();
  }

  @Override
  public <T> void findEachWhile(DocQueryRequest<T> query, Predicate<T> consumer) {
    throw implementationNotInClassPath();
  }

  @Override
  public void findEach(String indexNameType, String rawQuery, Consumer<RawDoc> consumer) {
    throw implementationNotInClassPath();
  }

  @Override
  public void findEachWhile(String indexNameType, String rawQuery, Predicate<RawDoc> consumer) {
    throw implementationNotInClassPath();
  }

  @Override
  public long process(List<DocStoreQueueEntry> queueEntries) throws IOException {
    throw implementationNotInClassPath();
  }
}
