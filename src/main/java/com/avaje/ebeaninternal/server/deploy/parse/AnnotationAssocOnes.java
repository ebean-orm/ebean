package com.avaje.ebeaninternal.server.deploy.parse;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
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
    public AnnotationAssocOnes(DeployBeanInfo<?> info, BeanDescriptorManager factory) {
        super(info);
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
            readEmbedded(embedded, prop);
        }
        EmbeddedId emId = get(prop, EmbeddedId.class);
        if (emId != null) {
            prop.setEmbedded(true);
            prop.setId(true);
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
            prop.setEmbedded(true);
            prop.setId(true);
            prop.setNullable(false);
        }

        Where where = get(prop, Where.class);
        if (where != null) {
            // not expecting this to be used on assoc one properties
            prop.setExtraWhere(where.clause());
        }

        if (validationAnnotations) {
          NotNull notNull = get(prop, NotNull.class);
          if (notNull != null) {
              prop.setNullable(false);
              // overrides optional attribute of ManyToOne etc
              prop.getTableJoin().setType(SqlJoinType.INNER);
          }
        }

        // check for manually defined joins
        BeanTable beanTable = prop.getBeanTable();
        JoinColumn joinColumn = get(prop, JoinColumn.class);
        if (joinColumn != null) {
          prop.getTableJoin().addJoinColumn(false, joinColumn, beanTable);
          if (!joinColumn.updatable()) {
            prop.setDbUpdateable(false);
          }
          if (!joinColumn.nullable()) {
            prop.setNullable(false);
          }
        }

        JoinColumns joinColumns = get(prop, JoinColumns.class);
        if (joinColumns != null) {
            prop.getTableJoin().addJoinColumn(false, joinColumns.value(), beanTable);
        }

        JoinTable joinTable = get(prop, JoinTable.class);
        if (joinTable != null) {
            prop.getTableJoin().addJoinColumn(false, joinTable.joinColumns(), beanTable);
        }

        info.setBeanJoinType(prop, prop.isNullable());

        if (!prop.getTableJoin().hasJoinColumns() && beanTable != null) {

            if (prop.getMappedBy() != null) {
                // the join is derived by reversing the join information
                // from the mapped by property.
                // Refer BeanDescriptorManager.readEntityRelationships()

            } else {
                // use naming convention to define join.
                NamingConvention nc = factory.getNamingConvention();
                
                String fkeyPrefix = null;
                if (nc.isUseForeignKeyPrefix()){
                    fkeyPrefix = nc.getColumnFromProperty(beanType, prop.getName());
                }

                beanTable.createJoinColumn(fkeyPrefix, prop.getTableJoin(), true);
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

        prop.setOneToOne(true);
        prop.setDbInsertable(true);
        prop.setDbUpdateable(true);
        prop.setNullable(propAnn.optional());
        prop.setFetchType(propAnn.fetch());
        prop.setMappedBy(propAnn.mappedBy());
        if (!"".equals(propAnn.mappedBy())) {
            prop.setOneToOneExported(true);
        }

        setCascadeTypes(propAnn.cascade(), prop.getCascadeInfo());

        BeanTable assoc = factory.getBeanTable(prop.getPropertyType());
        if (assoc == null) {
            String msg = errorMsgMissingBeanTable(prop.getPropertyType(), prop.getFullBeanName());
            throw new RuntimeException(msg);
        }

        prop.setBeanTable(assoc);
    }

    private void readEmbedded(Embedded propAnn, DeployBeanPropertyAssocOne<?> prop) {

        prop.setEmbedded(true);
        prop.setDbInsertable(true);
        prop.setDbUpdateable(true);

        EmbeddedColumns columns = get(prop, EmbeddedColumns.class);
        if (columns != null) {

            // convert into a Map
            String propColumns = columns.columns();
            Map<String, String> propMap = StringHelper.delimitedToMap(propColumns, ",", "=");

            prop.getDeployEmbedded().putAll(propMap);
        }

        readEmbeddedAttributeOverrides(prop);
    }

}
