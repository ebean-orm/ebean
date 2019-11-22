package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;

import java.util.UUID;

public class UuidGeneratedProperty implements GeneratedProperty {

  @Override
  public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
    return UUID.randomUUID();
  }

  @Override
  public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
    return null;
  }

  @Override
  public boolean includeInInsert() {
    return true;
  }

  @Override
  public boolean isDDLNotNullable() {
    return true;
  }

  @Override
  public boolean includeInUpdate() {
    return false;
  }

  @Override
  public boolean includeInAllUpdates() {
    return false;
  }

}
