package io.ebeanservice.docstore.api.mapping;

import io.ebean.annotation.DocMapping;
import io.ebean.annotation.DocProperty;

/**
 * Options for mapping a property for document storage.
 */
public class DocPropertyOptions {

  private Boolean code;

  private Boolean sortable;

  private Boolean store;

  private Float boost;

  private String nullValue;

  private Boolean includeInAll;

  private Boolean enabled;

  private Boolean norms;

  private Boolean docValues;

  private String analyzer;

  private String searchAnalyzer;

  private String copyTo;

  private DocProperty.Option options;

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
    this.includeInAll = source.includeInAll;
    this.analyzer = source.analyzer;
    this.searchAnalyzer = source.searchAnalyzer;
    this.options = source.options;
    this.docValues = source.docValues;
    this.norms = source.norms;
    this.copyTo = source.copyTo;
    this.enabled = source.enabled;
  }

  @Override
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

  public boolean isCode() {
    return Boolean.TRUE.equals(code);
  }

  public Boolean getCode() {
    return code;
  }

  public void setCode(Boolean code) {
    this.code = code;
  }

  public boolean isSortable() {
    return Boolean.TRUE.equals(sortable);
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

  public Boolean getIncludeInAll() {
    return includeInAll;
  }

  public void setIncludeInAll(Boolean includeInAll) {
    this.includeInAll = includeInAll;
  }

  public Boolean getDocValues() {
    return docValues;
  }

  public void setDocValues(Boolean docValues) {
    this.docValues = docValues;
  }

  public String getAnalyzer() {
    return analyzer;
  }

  public void setAnalyzer(String analyzer) {
    this.analyzer = analyzer;
  }

  public String getSearchAnalyzer() {
    return searchAnalyzer;
  }

  public void setSearchAnalyzer(String searchAnalyzer) {
    this.searchAnalyzer = searchAnalyzer;
  }

  public String getCopyTo() {
    return copyTo;
  }

  public void setCopyTo(String copyTo) {
    this.copyTo = copyTo;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Boolean getNorms() {
    return norms;
  }

  public void setNorms(Boolean norms) {
    this.norms = norms;
  }

  /**
   * Return true if the index options is set to a non-default value.
   */
  public boolean isOptionsSet() {
    return options != null && options != DocProperty.Option.DEFAULT;
  }

  public DocProperty.Option getOptions() {
    return options;
  }

  public void setOptions(DocProperty.Option options) {
    this.options = options;
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
    apply(docMapping.options());
  }

  /**
   * Apply the property level mapping options.
   */
  public void apply(DocProperty docMapping) {

    options = docMapping.options();
    if (docMapping.code()) {
      code = true;
    }
    if (docMapping.sortable()) {
      sortable = true;
    }
    if (docMapping.store()) {
      store = true;
    }
    if (Float.compare(docMapping.boost(), 1.0F) != 0) {
      boost = docMapping.boost();
    }
    if (!"".equals(docMapping.nullValue())) {
      nullValue = docMapping.nullValue();
    }
    if (!docMapping.includeInAll()) {
      includeInAll = false;
    }
    if (!docMapping.docValues()) {
      docValues = false;
    }
    if (!docMapping.enabled()) {
      enabled = false;
    }
    if (!docMapping.norms()) {
      norms = false;
    }
    if (!"".equals(docMapping.analyzer())) {
      analyzer = docMapping.analyzer();
    }
    if (!"".equals(docMapping.searchAnalyzer())) {
      searchAnalyzer = docMapping.searchAnalyzer();
    }
    if (!"".equals(docMapping.copyTo())) {
      copyTo = docMapping.copyTo();
    }
  }

}
