package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.annotation.DocCode;
import com.avaje.ebean.annotation.DocProperty;
import com.avaje.ebean.annotation.DocSortable;
import com.avaje.ebeanservice.docstore.api.mapping.DocPropertyOptions;

/**
 * The options for document property collected when reading deployment mapping.
 */
public class DeployDocPropertyOptions {

  private static DocPropertyOptions EMPTY = new DocPropertyOptions();

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
    mapping.setSortable(true);
    setStore(doc.store());
    setBoost(doc.boost());
    setNullValue(doc.nullValue());
  }

  /**
   * Read the DocCode deployment options.
   */
  public void setDocCode(DocCode doc) {

    createOptions();
    mapping.setCode(true);
    setStore(doc.store());
    setBoost(doc.boost());
    setNullValue(doc.nullValue());
  }

  private void setNullValue(String value) {
    if (!value.equals("")) {
      mapping.setNullValue(value);
    }
  }

  private void setBoost(float boost) {
    if (boost != 1) {
      mapping.setBoost(boost);
    }
  }

  private void setStore(boolean store) {
    if (store) {
      mapping.setStore(true);
    }
  }

  /**
   * Return the DocPropertyOptions with the collected options.
   */
  public DocPropertyOptions create() {
    return (mapping == null) ? EMPTY : mapping;
  }

}
