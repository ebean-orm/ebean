package com.avaje.ebeanservice.docstore.api.mapping;

import com.avaje.ebean.annotation.DocMapping;

/**
 * Options for mapping a property for document storage.
 */
public class DocPropertyOptions {

  private Boolean code;

  private Boolean sortable;

  private Boolean store;

  private Float boost;

  private String nullValue;

  /**
   * Construct with no values set.
   */
  public DocPropertyOptions() {

  }

  /**
   * Construct as a copy of the source options.
   */
  protected DocPropertyOptions(DocPropertyOptions source) {
    this.code = source.code;
    this.sortable = source.sortable;
    this.store = source.store;
    this.boost = source.boost;
    this.nullValue = source.nullValue;
  }

  /**
   * Construct with options set.
   */
  public DocPropertyOptions(Boolean code, Boolean sortable, Boolean store, Float boost, String nullValue) {
    this.code = code;
    this.sortable = sortable;
    this.store = store;
    this.boost = boost;
    this.nullValue = nullValue;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (code != null) {
      sb.append("code:").append(code).append(" ");
    }
    if (sortable != null) {
      sb.append("sortable:").append(sortable).append(" ");
    }
    if (store != null) {
      sb.append("store:").append(store).append(" ");
    }
    if (boost != null) {
      sb.append("boost:").append(boost).append(" ");
    }
    if (nullValue != null) {
      sb.append("nullValue:").append(nullValue).append(" ");
    }
    return sb.toString();
  }

  public Boolean getCode() {
    return code;
  }

  public void setCode(Boolean code) {
    this.code = code;
  }

  public Boolean getSortable() {
    return sortable;
  }

  public void setSortable(Boolean sortable) {
    this.sortable = sortable;
  }

  public Float getBoost() {
    return boost;
  }

  public void setBoost(Float boost) {
    this.boost = boost;
  }

  public String getNullValue() {
    return nullValue;
  }

  public void setNullValue(String nullValue) {
    this.nullValue = nullValue;
  }

  public Boolean getStore() {
    return store;
  }

  public void setStore(Boolean store) {
    this.store = store;
  }

  /**
   * Create a copy of this such that it can be overridden on a per index basis.
   */
  public DocPropertyOptions copy() {
    return new DocPropertyOptions(this);
  }

  /**
   * Apply override mapping from the document level or embedded property level.
   */
  public void apply(DocMapping docMapping) {

    if (docMapping.code()) {
      code = true;
    }
    if (docMapping.sortable()) {
      sortable = true;
    }
    if (docMapping.store()) {
      store = true;
    }
    if (docMapping.boost() != 1) {
      boost = docMapping.boost();
    }
    if (!"".equals(docMapping.nullValue())) {
      nullValue = docMapping.nullValue();
    }
  }

}
