package io.ebean.plugin;

import io.ebeaninternal.server.deploy.TableJoin;

public interface PropertyAssoc extends Property {
  
  TableJoin getTableJoin();

  BeanType<?> getTargetBeanType();
}
