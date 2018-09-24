package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;

public interface MatchedImportedProperty {

  /**
   * Populate the embeddedId bean from the source entity.
   */
  void populate(EntityBean sourceBean, EntityBean embeddedId);
}
