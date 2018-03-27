package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

/**
 * Matches local embedded id properties to 'matching' properties from a
 * ManyToOne associated bean that is a 'imported primary key'.
 */
class MatchedImportedEmbedded implements MatchedImportedProperty {

  private final BeanProperty localProp;

  private final BeanPropertyAssocOne<?> assocOne;

  private final BeanProperty foreignProp;

  MatchedImportedEmbedded(BeanProperty localProp, BeanPropertyAssocOne<?> assocOne, BeanProperty foreignProp) {
    this.localProp = localProp;
    this.assocOne = assocOne;
    this.foreignProp = foreignProp;
  }

  @Override
  public void populate(EntityBean sourceBean, EntityBean embeddedId) {
    Object assocBean = assocOne.getValue(sourceBean);
    if (assocBean == null) {
      throw new NullPointerException("The assoc bean for " + assocOne + " is null?");
    }
    Object value = foreignProp.getValue((EntityBean) assocBean);
    localProp.setValue(embeddedId, value);
  }

}
