package com.avaje.ebeaninternal.server.deploy.parse;

import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import com.avaje.ebean.annotation.EmbeddedColumns;
import com.avaje.ebean.annotation.Where;
import com.avaje.ebean.config.NamingConvention;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import com.avaje.ebeaninternal.server.deploy.BeanTable;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

/**
 * Read the deployment annotations for Associated One beans.
 */
public class AnnotationAssocOnes extends AnnotationParser {

  private final BeanDescriptorManager factory;

  /**
   * Create with the deploy Info.
   */
  public AnnotationAssocOnes(DeployBeanInfo<?> info, boolean javaxValidationAnnotations, BeanDescriptorManager factory) {
    super(info, javaxValidationAnnotations);
    this.factory = factory;
  }

  /**
   * Parse the annotation.
   */
  public void parse() {

    for (DeployBeanProperty prop : descriptor.propertiesAll()) {
      if (prop instanceof DeployBeanPropertyAssocOne<?>) {
        readAssocOne((DeployBeanPropertyAssocOne<?>) prop);
      }
    }
  }

  private void readAssocOne(DeployBeanPropertyAssocOne<?> prop) {

    ManyToOne manyToOne = get(prop, ManyToOne.class);
    if (manyToOne != null) {
      readManyToOne(manyToOne, prop);
    }
    OneToOne oneToOne = get(prop, OneToOne.class);
    if (oneToOne != null) {
      readOneToOne(oneToOne, prop);
    }
    Embedded embedded = get(prop, Embedded.class);
    if (embedded != null) {
      readEmbedded(prop);
    }
    EmbeddedId emId = get(prop, EmbeddedId.class);
    if (emId != null) {
      prop.setEmbedded();
      prop.setId();
      prop.setNullable(false);
    }
    Column column = get(prop, Column.class);
    if (column != null && !isEmpty(column.name())) {
      // have this in for AssocOnes used on
      // Sql based beans...
      prop.setDbColumn(column.name());
    }

    // May as well check for Id. Makes sense to me.
    Id id = get(prop, Id.class);
    if (id != null) {
      prop.setEmbedded();
      prop.setId();
      prop.setNullable(false);
    }

    Where where = get(prop, Where.class);
    if (where != null) {
      // not expecting this to be used on assoc one properties
      prop.setExtraWhere(where.clause());
    }

    if (validationAnnotations) {
      NotNull notNull = get(prop, NotNull.class);
      if (notNull != null && isEbeanValidationGroups(notNull.groups())) {
        prop.setNullable(false);
        // overrides optional attribute of ManyToOne etc
        prop.getTableJoin().setType(SqlJoinType.INNER);
      }
    }

    // check for manually defined joins
    BeanTable beanTable = prop.getBeanTable();
    for (JoinColumn joinColumn : getAll(prop, JoinColumn.class)) {
      prop.getTableJoin().addJoinColumn(false, joinColumn, beanTable);
      if (!joinColumn.updatable()) {
        prop.setDbUpdateable(false);
      }
      if (!joinColumn.nullable()) {
        prop.setNullable(false);
      }
    }


    JoinTable joinTable = get(prop, JoinTable.class);
    if (joinTable != null) {
      for (JoinColumn joinColumn : joinTable.joinColumns()) {
        prop.getTableJoin().addJoinColumn(false, joinColumn, beanTable);
        if (!joinColumn.updatable()) {
          prop.setDbUpdateable(false);
        }
        if (!joinColumn.nullable()) {
          prop.setNullable(false);
        }
      }
    }

    info.setBeanJoinType(prop, prop.isNullable());

    if (!prop.getTableJoin().hasJoinColumns() && beanTable != null) {

      //noinspection StatementWithEmptyBody
      if (prop.getMappedBy() != null) {
        // the join is derived by reversing the join information
        // from the mapped by property.
        // Refer BeanDescriptorManager.readEntityRelationships()

      } else {
        // use naming convention to define join.
        NamingConvention nc = factory.getNamingConvention();

        String fkeyPrefix = null;
        if (nc.isUseForeignKeyPrefix()) {
          fkeyPrefix = nc.getColumnFromProperty(beanType, prop.getName());
        }

        beanTable.createJoinColumn(fkeyPrefix, prop.getTableJoin(), true, prop.getSqlFormulaSelect());
      }
    }
  }

  private String errorMsgMissingBeanTable(Class<?> type, String from) {
    return "Error with association to [" + type + "] from [" + from + "]. Is " + type + " registered?";
  }

  private void readManyToOne(ManyToOne propAnn, DeployBeanProperty prop) {

    DeployBeanPropertyAssocOne<?> beanProp = (DeployBeanPropertyAssocOne<?>) prop;

    setCascadeTypes(propAnn.cascade(), beanProp.getCascadeInfo());

    BeanTable assoc = factory.getBeanTable(beanProp.getPropertyType());
    if (assoc == null) {
      String msg = errorMsgMissingBeanTable(beanProp.getPropertyType(), prop.getFullBeanName());
      throw new RuntimeException(msg);
    }
    beanProp.setBeanTable(assoc);
    beanProp.setDbInsertable(true);
    beanProp.setDbUpdateable(true);
    beanProp.setNullable(propAnn.optional());
    beanProp.setFetchType(propAnn.fetch());
  }

  private void readOneToOne(OneToOne propAnn, DeployBeanPropertyAssocOne<?> prop) {

    prop.setOneToOne();
    prop.setDbInsertable(true);
    prop.setDbUpdateable(true);
    prop.setNullable(propAnn.optional());
    prop.setFetchType(propAnn.fetch());
    prop.setMappedBy(propAnn.mappedBy());
    if (!"".equals(propAnn.mappedBy())) {
      prop.setOneToOneExported();
    }

    setCascadeTypes(propAnn.cascade(), prop.getCascadeInfo());

    BeanTable assoc = factory.getBeanTable(prop.getPropertyType());
    if (assoc == null) {
      String msg = errorMsgMissingBeanTable(prop.getPropertyType(), prop.getFullBeanName());
      throw new RuntimeException(msg);
    }

    prop.setBeanTable(assoc);
  }

  private void readEmbedded(DeployBeanPropertyAssocOne<?> prop) {

    prop.setEmbedded();
    prop.setDbInsertable(true);
    prop.setDbUpdateable(true);

    EmbeddedColumns columns = get(prop, EmbeddedColumns.class);
    if (columns != null) {

      // convert into a Map
      String propColumns = columns.columns();
      Map<String, String> propMap = StringHelper.delimitedToMap(propColumns, ",", "=");

      // replace entries like *=foo_* with all mappings from entity
      String wildcardMapping = propMap.remove("*");
      if (wildcardMapping != null) {
        for (String targetProp : prop.getTargetDeploy().getProperties()) {
          if (!propMap.containsKey(targetProp)) {
            NamingConvention nc = factory.getNamingConvention();
            String propName = nc.getColumnFromProperty(prop.getTargetDeploy().getBeanType(), targetProp);
            propMap.put(targetProp, StringHelper.replaceString(wildcardMapping, "*", propName));
          }
        }
      }
      prop.getDeployEmbedded().putAll(propMap);
    }

    readEmbeddedAttributeOverrides(prop);
  }

}
