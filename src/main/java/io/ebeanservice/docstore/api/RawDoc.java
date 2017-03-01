package io.ebeanservice.docstore.api;

import java.util.Map;

/**
 * Raw document.
 */
public class RawDoc {

  private Map<String, Object> source;
  private String id;
  private double score;
  private String index;
  private String type;

  /**
   * Construct the document with all the meta data.
   */
  public RawDoc(Map<String, Object> source, String id, double score, String index, String type) {
    this.source = source;
    this.id = id;
    this.score = score;
    this.index = index;
    this.type = type;
  }

  /**
   * Construct empty (typically for JSON marshalling).
   */
  public RawDoc() {
  }

  /**
   * Return the source document as a Map.
   */
  public Map<String, Object> getSource() {
    return source;
  }

  /**
   * Return the Id value.
   */
  public String getId() {
    return id;
  }

  /**
   * Return the score.
   */
  public double getScore() {
    return score;
  }

  /**
   * Return the index name.
   */
  public String getIndex() {
    return index;
  }

  /**
   * Return the index type.
   */
  public String getType() {
    return type;
  }

  /**
   * Set the source document.
   */
  public void setSource(Map<String, Object> source) {
    this.source = source;
  }

  /**
   * Set the id value.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Set the score.
   */
  public void setScore(double score) {
    this.score = score;
  }

  /**
   * Set the index name.
   */
  public void setIndex(String index) {
    this.index = index;
  }

  /**
   * Set the index type.
   */
  public void setType(String type) {
    this.type = type;
  }
}
