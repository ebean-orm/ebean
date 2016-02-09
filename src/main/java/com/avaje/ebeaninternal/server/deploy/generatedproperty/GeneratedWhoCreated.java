package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.config.CurrentUserProvider;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to populate @WhoCreated bean properties.
 */
public class GeneratedWhoCreated implements GeneratedProperty {

  final CurrentUserProvider currentUserProvider;

  public GeneratedWhoCreated(CurrentUserProvider currentUserProvider) {
    this.currentUserProvider = currentUserProvider;
  }

  @Override
  public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
    return currentUserProvider.currentUser();
  }

  @Override
  public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
    return null;
  }

  @Override
  public boolean includeInUpdate() {
    return false;
  }

  @Override
  public boolean includeInAllUpdates() {
    return false;
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
