package io.ebean.config;

import io.ebean.Transaction;
import io.ebean.annotation.DocStoreMode;

/**
 * Configuration for the Document store integration (e.g. ElasticSearch).
 */
public class DocStoreConfig {

  /**
   * True when the Document store integration is active/on.
   */
  protected boolean active;

  /**
   * Set to true means Ebean will generate mapping files on startup.
   */
  protected boolean generateMapping;

  /**
   * When true the Document store should drop and re-create document indexes.
   */
  protected boolean dropCreate;

  /**
   * When true the Document store should create any document indexes that don't already exist.
   */
  protected boolean create;

  /**
   * The URL of the Document store server. For example: http://localhost:9200.
   */
  protected String url;

  /**
   * Credential that be used for authentication to document store.
   */
  protected String username;

  /**
   * Password credential that be used for authentication to document store.
   */
  protected String password;

  /**
   * Set to true such that the client allows connections to invalid/self signed SSL certificates.
   */
  protected boolean allowAllCertificates;

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
   * Return the user credential for connecting to the document store.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Set the user credential for connecting to the document store.
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Return the password credential for connecting to the document store.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Set the password credential for connecting to the document store.
   */
  public void setPassword(String password) {
    this.password = password;
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
   * Return true if Ebean should generate mapping files on server startup.
   */
  public boolean isGenerateMapping() {
    String systemValue = System.getProperty("ebean.docstore.generateMapping");
    if (systemValue != null) {
      return Boolean.parseBoolean(systemValue);
    }
    return generateMapping;
  }

  /**
   * Set to true if Ebean should generate mapping files on server startup.
   */
  public void setGenerateMapping(boolean generateMapping) {
    this.generateMapping = generateMapping;
  }

  /**
   * Return true if the document store should recreate mapped indexes.
   */
  public boolean isDropCreate() {
    String systemValue = System.getProperty("ebean.docstore.dropCreate");
    if (systemValue != null) {
      return Boolean.parseBoolean(systemValue);
    }
    return dropCreate;
  }

  /**
   * Set to true if the document store should recreate mapped indexes.
   */
  public void setDropCreate(boolean dropCreate) {
    this.dropCreate = dropCreate;
  }

  /**
   * Create true if the document store should create mapped indexes that don't yet exist.
   * This is only used if dropCreate is false.
   */
  public boolean isCreate() {
    String systemValue = System.getProperty("ebean.docstore.create");
    if (systemValue != null) {
      return Boolean.parseBoolean(systemValue);
    }
    return create;
  }

  /**
   * Set to true if the document store should create mapped indexes that don't yet exist.
   * This is only used if dropCreate is false.
   */
  public void setCreate(boolean create) {
    this.create = create;
  }

  /**
   * Return true if the client allows connections to invalid/self signed SSL certificates.
   */
  public boolean isAllowAllCertificates() {
    return allowAllCertificates;
  }

  /**
   * Set to true such that the client allows connections to invalid/self signed SSL certificates.
   */
  public void setAllowAllCertificates(boolean allowAllCertificates) {
    this.allowAllCertificates = allowAllCertificates;
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
   * The batch size can be set on a transaction via {@link Transaction#setDocStoreBatchSize(int)}.
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
    username = properties.get("docstore.username", url);
    password = properties.get("docstore.password", url);
    persist = properties.getEnum(DocStoreMode.class, "docstore.persist", persist);
    bulkBatchSize = properties.getInt("docstore.bulkBatchSize", bulkBatchSize);
    generateMapping = properties.getBoolean("docstore.generateMapping", generateMapping);
    dropCreate = properties.getBoolean("docstore.dropCreate", dropCreate);
    create = properties.getBoolean("docstore.create", create);
    allowAllCertificates = properties.getBoolean("docstore.allowAllCertificates", allowAllCertificates);
    mappingPath = properties.get("docstore.mappingPath", mappingPath);
    mappingSuffix = properties.get("docstore.mappingSuffix", mappingSuffix);
    pathToResources = properties.get("docstore.pathToResources", pathToResources);
  }
}
