package io.ebeaninternal.server.deploy;

import io.ebean.annotation.DocCode;
import io.ebean.annotation.DocProperty;
import io.ebean.annotation.DocSortable;
import io.ebeanservice.docstore.api.mapping.DocPropertyOptions;

/**
 * The options for document property collected when reading deployment mapping.
 */
public final class DeployDocPropertyOptions {

  private static final DocPropertyOptions EMPTY = new DocPropertyOptions();

  private DocPropertyOptions mapping;

  private void createOptions() {
    if (mapping == null) {
      mapping = new DocPropertyOptions();
    }
  }

  /**
   * Read the DocProperty deployment options.
   */
  public void setDocProperty(DocProperty doc) {
    createOptions();
    mapping.apply(doc);
  }

  /**
   * Read the DocSortable deployment options.
   */
  public void setDocSortable(DocSortable doc) {
    createOptions();
    mapping.sortable(true);
    setStore(doc.store());
    setBoost(doc.boost());
    setNullValue(doc.nullValue());
  }

  /**
   * Read the DocCode deployment options.
   */
  public void setDocCode(DocCode doc) {
    createOptions();
    mapping.code(true);
    setStore(doc.store());
    setBoost(doc.boost());
    setNullValue(doc.nullValue());
  }

  private void setNullValue(String value) {
    if (!value.isEmpty()) {
      mapping.nullValue(value);
    }
  }

  private void setBoost(float boost) {
    if (Float.compare(boost, 1.0F) != 0) {
      mapping.boost(boost);
    }
  }

  private void setStore(boolean store) {
    if (store) {
      mapping.store(true);
    }
  }

  /**
   * Return the DocPropertyOptions with the collected options.
   */
  public DocPropertyOptions create() {
    return (mapping == null) ? EMPTY : mapping;
  }

}
