package io.ebeanservice.docstore.api.mapping;

import io.ebean.FetchPath;
import io.ebean.docstore.DocMapping;

/**
 * Mapping for a document stored in a doc store (like ElasticSearch).
 */
public final class DocumentMapping implements DocMapping {

  protected final String queueId;
  protected final String name;
  protected final String type;
  protected final FetchPath paths;
  protected final DocPropertyMapping properties;
  protected int shards;
  protected int replicas;

  public DocumentMapping(String queueId, String name, String type, FetchPath paths, DocPropertyMapping properties, int shards, int replicas) {
    this.queueId = queueId;
    this.name = name;
    this.type = type;
    this.paths = paths;
    this.properties = properties;
    this.shards = shards;
    this.replicas = replicas;
  }

  /**
   * Visit all the properties in the document structure.
   */
  public void visit(DocPropertyVisitor visitor) {
    properties.visit(visitor);
  }

  /**
   * Return the queueId.
   */
  public String queueId() {
    return queueId;
  }

  /**
   * Return the name.
   */
  public String name() {
    return name;
  }

  /**
   * Return the type.
   */
  public String type() {
    return type;
  }

  /**
   * Return the document structure as PathProperties.
   */
  public FetchPath paths() {
    return paths;
  }

  /**
   * Return the document structure with mapping details.
   */
  public DocPropertyMapping properties() {
    return properties;
  }

  /**
   * Return the number of shards.
   */
  public int shards() {
    return shards;
  }

  /**
   * Set the number of shards.
   */
  public void shards(int shards) {
    this.shards = shards;
  }

  /**
   * Return the number of replicas.
   */
  public int replicas() {
    return replicas;
  }

  /**
   * Set the number of replicas.
   */
  public void replicas(int replicas) {
    this.replicas = replicas;
  }
}
