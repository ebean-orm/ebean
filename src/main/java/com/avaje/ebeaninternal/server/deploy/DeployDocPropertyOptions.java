package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.annotation.DocCode;
import com.avaje.ebean.annotation.DocProperty;
import com.avaje.ebean.annotation.DocSortable;
import com.avaje.ebeanservice.docstore.api.mapping.DocPropertyOptions;

/**
 * The options for document property collected when reading deployment mapping.
 */
public class DeployDocPropertyOptions {

  private Boolean code;

  private Boolean sortable;

  private Boolean store;

  private Float boost;

  private String nullValue;

  /**
   * Read the DocProperty deployment options.
   */
  public void setDocProperty(DocProperty doc) {
    code = checkDefault(doc.code());
    sortable = checkDefault(doc.sortable());
    store = checkDefault(doc.store());
    boost = checkDefault(doc.boost());
    nullValue = checkDefault(doc.nullValue());
  }

  /**
   * Read the DocSortable deployment options.
   */
  public void setDocSortable(DocSortable doc) {
    sortable = Boolean.TRUE;
    store = checkDefault(doc.store());
    boost = checkDefault(doc.boost());
    nullValue = checkDefault(doc.nullValue());
  }

  /**
   * Read the DocCode deployment options.
   */
  public void setDocCode(DocCode doc) {
    code = Boolean.TRUE;
    store = checkDefault(doc.store());
    boost = checkDefault(doc.boost());
    nullValue = checkDefault(doc.nullValue());
  }

  private String checkDefault(String s) {
    return "".equals(s) ? null : s;
  }

  private Float checkDefault(float boost) {
    return (boost == 1) ? null : boost;
  }

  private Boolean checkDefault(boolean store) {
    return (store) ? Boolean.TRUE : null;
  }

  /**
   * Return the DocPropertyOptions with the collected options.
   */
  public DocPropertyOptions create() {
    return new DocPropertyOptions(code, sortable, store, boost, nullValue);
  }

}
