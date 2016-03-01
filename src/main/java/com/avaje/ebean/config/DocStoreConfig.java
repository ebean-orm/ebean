package com.avaje.ebean.config;

import com.avaje.ebean.annotation.DocStoreEvent;

/**
 * Configuration for the Document store (ElasticSearch) integration.
 */
public class DocStoreConfig {

  /**
   * True when the Document store integration is active/on.
   */
  boolean active;

  /**
   * When true the Document store should drop and re-create any document mapping (like DDL).
   */
  boolean dropCreate;

  /**
   * The URL of the Document store server. For example: http://localhost:9200.
   */
  String url;

  /**
   * The default mode used by indexes.
   */
  DocStoreEvent persist = DocStoreEvent.UPDATE;

  /**
   * The default batch size to use for the Bulk API calls.
   */
  int bulkBatchSize = 1000;


  /**
   * Return true if the Document store (ElasticSearch) integration is active.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Set to true to make the Document store (ElasticSearch) integration active.
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Return true if the document store should recreate mappings.
   */
  public boolean isDropCreate() {
    return dropCreate;
  }

  /**
   * Set to true if the document store should recreate mappings.
   */
  public void setDropCreate(boolean dropCreate) {
    this.dropCreate = dropCreate;
  }

  /**
   * Return the default behavior for when Insert, Update and Delete events occur on beans that have an associated
   * Document store.
   */
  public DocStoreEvent getPersist() {
    return persist;
  }

  /**
   * Set the default behavior for when Insert, Update and Delete events occur on beans that have an associated
   * Document store.
   * <ul>
   * <li>DocStoreEvent.UPDATE - build and send message to Bulk API</li>
   * <li>DocStoreEvent.QUEUE - add an entry with the index type and id only into a queue for later processing</li>
   * <li>DocStoreEvent.IGNORE - ignore. Most likely used when some scheduled batch job handles updating the index</li>
   * </ul>
   * <p>
   * You might choose to use QUEUE if that particular index data is updating very frequently or the cost of indexing
   * is expensive.  Setting it to QUEUE can mean many changes can be batched together potentially coalescing multiple
   * updates for an index entry into a single update.
   * </p>
   * <p>
   * You might choose to use IGNORE when you have your own external process for updating the indexes. In this case
   * you don't want Ebean to do anything when the data changes.
   * </p>
   */
  public void setPersist(DocStoreEvent persist) {
    this.persist = persist;
  }

  /**
   * Return the URL to the Document store.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Set the URL to the Document store server.
   *
   * For a local ElasticSearch server this would be: http://localhost:9200
   */
  public void setUrl(String url) {
    this.url = url;
  }


  /**
   * Return the default batch size to use for calls to the Bulk API.
   */
  public int getBulkBatchSize() {
    return bulkBatchSize;
  }

  /**
   * Set the default batch size to use for calls to the Bulk API.
   * <p>
   *   The batch size can be set on a transaction via {@link com.avaje.ebean.Transaction#setDocStoreUpdateBatchSize(int)}.
   * </p>
   */
  public void setBulkBatchSize(int bulkBatchSize) {
    this.bulkBatchSize = bulkBatchSize;
  }

  /**
   * Load settings specified in properties files.
   */
  public void loadSettings(PropertiesWrapper properties) {

    active = properties.getBoolean("docstore.active", active);
    url = properties.get("docstore.url", url);
    persist = properties.getEnum(DocStoreEvent.class, "docstore.persist", persist);
    bulkBatchSize = properties.getInt("docstore.bulkBatchSize", bulkBatchSize);
  }
}
