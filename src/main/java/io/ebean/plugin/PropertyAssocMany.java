package io.ebean.plugin;

import io.ebeaninternal.server.deploy.TableJoin;

public interface PropertyAssocMany extends PropertyAssoc {

  boolean isManyToMany();

  Object getMappedBy();

  TableJoin getIntersectionTableJoin();

}
