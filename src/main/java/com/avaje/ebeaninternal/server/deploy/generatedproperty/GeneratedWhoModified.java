package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.config.CurrentUserProvider;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to populate @WhoModified bean properties.
 */
public class GeneratedWhoModified implements GeneratedProperty {

  final CurrentUserProvider currentUserProvider;

  public GeneratedWhoModified(CurrentUserProvider currentUserProvider) {
    this.currentUserProvider = currentUserProvider;
  }

  @Override
  public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
    return currentUserProvider.currentUser();
  }

  @Override
  public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
    return currentUserProvider.currentUser();
  }

  @Override
  public boolean includeInUpdate() {
    return true;
  }

  @Override
  public boolean includeInAllUpdates() {
    return true;
  }

  @Override
  public boolean includeInInsert() {
    return true;
  }

  @Override
  public boolean isDDLNotNullable() {
    return true;
  }
}
