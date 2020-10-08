package io.ebeaninternal.server.deploy.parse;

import io.ebean.RawSql;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.rawsql.SpiRawSql;

/**
 * Wraps information about a bean during deployment parsing.
 */
public class DeployBeanInfo<T> {

  private final DeployUtil util;

  private final DeployBeanDescriptor<T> descriptor;

  private DeployBeanPropertyAssoc<?> embeddedId;

  /**
   * Create with a DeployUtil and BeanDescriptor.
   */
  public DeployBeanInfo(DeployUtil util, DeployBeanDescriptor<T> descriptor) {
    this.util = util;
    this.descriptor = descriptor;
  }

  @Override
  public String toString() {
    return String.valueOf(descriptor);
  }

  /**
   * Return the BeanDescriptor currently being processed.
   */
  public DeployBeanDescriptor<T> getDescriptor() {
    return descriptor;
  }

  /**
   * Return the DeployUtil we are using.
   */
  public DeployUtil getUtil() {
    return util;
  }

  /**
   * Add named RawSql from ebean.xml.
   */
  public void addRawSql(String name, RawSql rawSql) {
    descriptor.addRawSql(name, (SpiRawSql)rawSql);
  }

  /**
   * Add the named query.
   */
  public void addNamedQuery(String name, String query) {
    descriptor.addNamedQuery(name, query);
  }

  /**
   * Set that the PK is also a foreign key.
   */
  public void setPrimaryKeyJoin(TableJoin join) {
    descriptor.setPrimaryKeyJoin(join);
  }

  /**
   * This bean type has an embedded Id property.
   */
  public void setEmbeddedId(DeployBeanPropertyAssoc<?> embeddedId) {
    this.embeddedId = embeddedId;
  }

  public Class<?> getEmbeddedIdType() {
    return (embeddedId == null) ? null : embeddedId.getTargetType();
  }

  public boolean isEmbedded() {
    return descriptor.isEmbedded();
  }
}
