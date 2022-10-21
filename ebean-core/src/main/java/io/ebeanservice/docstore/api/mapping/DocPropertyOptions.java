package io.ebeanservice.docstore.api.mapping;

import io.ebean.annotation.DocMapping;
import io.ebean.annotation.DocProperty;

/**
 * Options for mapping a property for document storage.
 */
public final class DocPropertyOptions {

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
  DocPropertyOptions(DocPropertyOptions source) {
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

  public Boolean code() {
    return code;
  }

  public void code(Boolean code) {
    this.code = code;
  }

  public boolean isSortable() {
    return Boolean.TRUE.equals(sortable);
  }

  public Boolean sortable() {
    return sortable;
  }

  public void sortable(Boolean sortable) {
    this.sortable = sortable;
  }

  public Float boost() {
    return boost;
  }

  public void boost(Float boost) {
    this.boost = boost;
  }

  public String nullValue() {
    return nullValue;
  }

  public void nullValue(String nullValue) {
    this.nullValue = nullValue;
  }

  public Boolean store() {
    return store;
  }

  public void store(Boolean store) {
    this.store = store;
  }

  public Boolean includeInAll() {
    return includeInAll;
  }

  public void includeInAll(Boolean includeInAll) {
    this.includeInAll = includeInAll;
  }

  public Boolean docValues() {
    return docValues;
  }

  public void docValues(Boolean docValues) {
    this.docValues = docValues;
  }

  public String analyzer() {
    return analyzer;
  }

  public void analyzer(String analyzer) {
    this.analyzer = analyzer;
  }

  public String searchAnalyzer() {
    return searchAnalyzer;
  }

  public void searchAnalyzer(String searchAnalyzer) {
    this.searchAnalyzer = searchAnalyzer;
  }

  public String copyTo() {
    return copyTo;
  }

  public void copyTo(String copyTo) {
    this.copyTo = copyTo;
  }

  public Boolean enabled() {
    return enabled;
  }

  public void enabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Boolean norms() {
    return norms;
  }

  public void norms(Boolean norms) {
    this.norms = norms;
  }

  /**
   * Return true if the index options is set to a non-default value.
   */
  public boolean isOptionsSet() {
    return options != null && options != DocProperty.Option.DEFAULT;
  }

  public DocProperty.Option options() {
    return options;
  }

  public void options(DocProperty.Option options) {
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
