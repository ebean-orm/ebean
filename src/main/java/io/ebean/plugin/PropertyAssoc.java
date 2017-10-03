package io.ebean.plugin;


public interface PropertyAssoc extends Property {
  
  TableJoinInfo getTableJoin();

  BeanType<?> getTargetBeanType();
}
