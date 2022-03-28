package io.ebean;

import java.util.Collection;
import java.util.Map;

import io.ebean.bean.EntityBean;
import io.ebean.plugin.Property;

@FunctionalInterface
public interface PersistVisitor {

  PersistVisitor visitBean(EntityBean bean);

  default PersistVisitor visitProperty(Property prop) {
    return this;
  };

  default PersistVisitor visitCollection(Collection<?> collection) {
    return this;
  };

  default PersistVisitor visitMap(Map<?, ?> map) {
    return this;
  };

  default void visitEnd() {
  }

}
