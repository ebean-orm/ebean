package com.avaje.ebean.config;

import com.avaje.ebean.annotation.DocStoreMode;

/**
 * Configuration for the Document store integration (e.g. ElasticSearch).
 */
public class DocStoreConfig {

  /**
   * True when the Document store integration is active/on.
   */
  protected boolean active;

  /**
   * When true the Document store should drop and re-create any document mapping (like DDL).
   */
  protected boolean dropCreate;

  /**
   * The URL of the Document store server. For example: http://localhost:9200.
   */
  protected String url;

  /**
   * The default mode used by indexes.
   */
  protected DocStoreMode persist = DocStoreMode.UPDATE;

  /**
   * The default batch size to use for the Bulk API calls.
   */
  protected int bulkBatchSize = 1000;

  /**
   * Resource path for the Document store mapping files.
   */
  protected String mappingPath;

  /**
   * Suffix used for mapping files.
   */
  protected String mappingSuffix;

  /**
   * Location of resources that mapping files are generated into.
   */
  protected String pathToResources = "src/main/resources";

  /**
   * Return true if the Document store (ElasticSearch) integration is active.
   */
  public boolean isActive() {
    String systemValue = System.getProperty("ebean.docstore.active");
    if (systemValue != null) {
      return Boolean.parseBoolean(systemValue);
    }
    return active;
  }

  /**
   * Set to true to make the Document store (ElasticSearch) integration active.
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Return the URL to the Document store.
   */
  public String getUrl() {
    String systemValue = System.getProperty("ebean.docstore.url");
    if (systemValue != null) {
      return systemValue;
    }

    return url;
  }

  /**
   * Set the URL to the Document store server.
   * <p>
   * For a local ElasticSearch server this would be: http://localhost:9200
   */
  public void setUrl(String url) {
    this.url = url;
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
   * Return the default batch size to use for calls to the Bulk API.
   */
  public int getBulkBatchSize() {
    return bulkBatchSize;
  }

  /**
   * Set the default batch size to use for calls to the Bulk API.
   * <p>
   * The batch size can be set on a transaction via {@link com.avaje.ebean.Transaction#setDocStoreBatchSize(int)}.
   * </p>
   */
  public void setBulkBatchSize(int bulkBatchSize) {
    this.bulkBatchSize = bulkBatchSize;
  }

  /**
   * Return the mapping path.
   */
  public String getMappingPath() {
    return mappingPath;
  }

  /**
   * Set the mapping path.
   */
  public void setMappingPath(String mappingPath) {
    this.mappingPath = mappingPath;
  }

  /**
   * Return the mapping suffix.
   */
  public String getMappingSuffix() {
    return mappingSuffix;
  }

  /**
   * Set the mapping suffix.
   */
  public void setMappingSuffix(String mappingSuffix) {
    this.mappingSuffix = mappingSuffix;
  }

  /**
   * Return the relative file system path to resources when generating mapping files.
   */
  public String getPathToResources() {
    return pathToResources;
  }

  /**
   * Set the relative file system path to resources when generating mapping files.
   */
  public void setPathToResources(String pathToResources) {
    this.pathToResources = pathToResources;
  }

  /**
   * Return the default behavior for when Insert, Update and Delete events occur on beans that have an associated
   * Document store.
   */
  public DocStoreMode getPersist() {
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
  public void setPersist(DocStoreMode persist) {
    this.persist = persist;
  }

  /**
   * Load settings specified in properties files.
   */
  public void loadSettings(PropertiesWrapper properties) {

    active = properties.getBoolean("docstore.active", active);
    url = properties.get("docstore.url", url);
    persist = properties.getEnum(DocStoreMode.class, "docstore.persist", persist);
    bulkBatchSize = properties.getInt("docstore.bulkBatchSize", bulkBatchSize);
    dropCreate = properties.getBoolean("docstore.dropCreate", dropCreate);
    mappingPath = properties.get("docstore.mappingPath", mappingPath);
    mappingSuffix = properties.get("docstore.mappingSuffix", mappingSuffix);
    pathToResources = properties.get("docstore.pathToResources", pathToResources);
  }
}
