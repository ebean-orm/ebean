package io.ebeaninternal.server.query;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.TableJoin;

public interface STreePropertyAssocMany extends STreePropertyAssoc {

  /**
   * Return as BeanPropertyAssocMany.
   */
  BeanPropertyAssocMany<?> asMany();

  /**
   * Append exported columns to the select.
   */
  void addSelectExported(DbSqlContext ctx, String prefix);

  /**
   * Return true if this is a ManyToMany with history.
   */
  boolean isManyToManyWithHistory();

  /**
   * Return a reference collection.
   */
  BeanCollection<?> createReferenceIfNull(EntityBean localBean);

  /**
   * Return a reference collection forcing a new reference on REFRESH query.
   */
  BeanCollection<?> createReference(EntityBean localBean, boolean forceNewReference);

  /**
   * Populate the collection for read-only disabled lazy loading (aka Java Collections non-mutable empty collection).
   */
  void createEmptyReference(EntityBean localBean);

  /**
   * Return true if the property has a join table.
   */
  boolean hasJoinTable();

  /**
   * Return the intersection table join.
   */
  TableJoin intersectionTableJoin();

  /**
   * Add a bean to the collection.
   */
  void addBeanToCollectionWithCreate(EntityBean contextParent, EntityBean detailBean, boolean withCheck);

  /**
   * Return true if the property is excluded from history.
   */
  boolean isExcludedFromHistory();

}
