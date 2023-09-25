package io.ebeaninternal.server.persist.dmlbind;

import io.ebeaninternal.server.core.PersistRequestBean;

/**
 * Adds support for id creation for concatenated ids on intersection tables.
 * <p>
 * Specifically if the concatenated id object is null on insert this can be
 * built from the matching ManyToOne associated beans. For example RoleUserId
 * embeddedId object could be built from the associated Role and User beans.
 * <p>
 * This is only attempted if the id is null when it gets to the insert.
 */
public interface BindableId extends Bindable {

  /**
   * Return true if there is no Id properties at all.
   */
  boolean isEmpty();

  /**
   * Return true if this is a concatenated key.
   */
  boolean isConcatenated();

  /**
   * Return the DB Column to use with genGeneratedKeys.
   */
  String getIdentityColumn();

  /**
   * Create the concatenated id for inserts with PFK relationships.
   * <p>
   * Really only where there are ManyToOne assoc beans that make up the
   * primary key and the values can be got from those.
   */
  boolean deriveConcatenatedId(PersistRequestBean<?> persist);

}
