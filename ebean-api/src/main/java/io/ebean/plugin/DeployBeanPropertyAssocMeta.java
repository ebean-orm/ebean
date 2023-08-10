package io.ebean.plugin;

public interface DeployBeanPropertyAssocMeta extends DeployBeanPropertyMeta {

  /**
   * Return the mappedBy deployment attribute.
   * <p>
   * This is the name of the property in the 'detail' bean that maps back to
   * this 'master' bean.
   * </p>
   */
  String getMappedBy();

  /**
   * Return the base table for this association.
   * <p>
   * This has the table name which is used to determine the relationship for
   * this association.
   * </p>
   */
  String getBaseTable();

}
