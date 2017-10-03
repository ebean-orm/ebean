package io.ebean.plugin;


public interface PropertyAssocMany extends PropertyAssoc {

  boolean isManyToMany();

  Object getMappedBy();

  TableJoinInfo getIntersectionTableJoin();

}
