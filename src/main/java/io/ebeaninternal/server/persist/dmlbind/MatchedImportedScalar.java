package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;

/**
 * Matches local embedded id properties to 'matching' imported primary key scalar properties.
 */
class MatchedImportedScalar implements MatchedImportedProperty {

  private final BeanProperty localProp;

  private final BeanProperty foreignProp;

  MatchedImportedScalar(BeanProperty localProp, BeanProperty foreignProp) {
    this.localProp = localProp;
    this.foreignProp = foreignProp;
  }

  @Override
  public void populate(EntityBean sourceBean, EntityBean embeddedId) {

    Object value = foreignProp.getValue(sourceBean);
    localProp.setValue(embeddedId, value);
  }

}
