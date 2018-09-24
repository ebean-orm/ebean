package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.DbForeignKey;
import io.ebean.annotation.FetchPreference;
import io.ebean.annotation.TenantId;
import io.ebean.annotation.Where;
import io.ebean.config.NamingConvention;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.BeanTable;
import io.ebeaninternal.server.deploy.PropertyForeignKey;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import io.ebeaninternal.server.query.SqlJoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

/**
 * Read the deployment annotations for Associated One beans.
 */
public class AnnotationAssocOnes extends AnnotationParser {

  private static final Logger log = LoggerFactory.getLogger(AnnotationAssocOnes.class);

  private final BeanDescriptorManager factory;

  /**
   * Create with the deploy Info.
   */
  AnnotationAssocOnes(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig, BeanDescriptorManager factory) {
    super(info, readConfig);
    this.factory = factory;
  }

  /**
   * Parse the annotation.
   */
  @Override
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
      if (get(prop, TenantId.class) != null) {
        prop.setTenantId();
      }
    }
    OneToOne oneToOne = get(prop, OneToOne.class);
    if (oneToOne != null) {
      readOneToOne(oneToOne, prop);
    }
    Embedded embedded = get(prop, Embedded.class);
    if (embedded != null) {
      readEmbedded(prop, embedded);
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
      readIdAssocOne(prop);
    }

    DbForeignKey dbForeignKey = get(prop, DbForeignKey.class);
    if (dbForeignKey != null){
      prop.setForeignKey(new PropertyForeignKey(dbForeignKey));
    }

    Where where = get(prop, Where.class);
    if (where != null) {
      // not expecting this to be used on assoc one properties
      prop.setExtraWhere(where.clause());
    }

    PrimaryKeyJoinColumn primaryKeyJoin = get(prop, PrimaryKeyJoinColumn.class);
    if (primaryKeyJoin != null) {
      readPrimaryKeyJoin(primaryKeyJoin, prop);
    }

    FetchPreference fetchPreference = get(prop, FetchPreference.class);
    if (fetchPreference != null) {
      prop.setFetchPreference(fetchPreference.value());
    }

    io.ebean.annotation.NotNull nonNull = get(prop, io.ebean.annotation.NotNull.class);
    if (nonNull != null) {
      prop.setNullable(false);
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
      if (beanTable == null) {
        throw new IllegalStateException("Looks like a missing @ManyToOne or @OneToOne on property " + prop.getFullBeanName()+" - no related 'BeanTable'");
      }
      prop.getTableJoin().addJoinColumn(false, joinColumn, beanTable);
      if (!joinColumn.updatable()) {
        prop.setDbUpdateable(false);
      }
      if (!joinColumn.nullable()) {
        prop.setNullable(false);
      }
      checkForNoConstraint(prop, joinColumn);
    }


    JoinTable joinTable = get(prop, JoinTable.class);
    if (joinTable != null) {
      for (JoinColumn joinColumn : joinTable.joinColumns()) {
        if (beanTable == null) {
          throw new IllegalStateException("Looks like a missing @ManyToOne or @OneToOne on property " + prop.getFullBeanName()+" - no related 'BeanTable'");
        }
        prop.getTableJoin().addJoinColumn(false, joinColumn, beanTable);
        if (!joinColumn.updatable()) {
          prop.setDbUpdateable(false);
        }
        if (!joinColumn.nullable()) {
          prop.setNullable(false);
        }
      }
    }

    prop.setJoinType(prop.isNullable());

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

  private void checkForNoConstraint(DeployBeanPropertyAssocOne<?> prop, JoinColumn joinColumn) {
    ForeignKey foreignKey = joinColumn.foreignKey();
    if (foreignKey != null && foreignKey.value() == ConstraintMode.NO_CONSTRAINT) {
      prop.setForeignKey(new PropertyForeignKey());
    }
  }

  private String errorMsgMissingBeanTable(Class<?> type, String from) {
    return "Error with association to [" + type + "] from [" + from + "]. Is " + type + " registered? Does it have the @Entity annotation?";
  }

  private BeanTable beanTable(DeployBeanPropertyAssoc<?> prop) {
    BeanTable assoc = factory.getBeanTable(prop.getPropertyType());
    if (assoc == null) {
      throw new RuntimeException(errorMsgMissingBeanTable(prop.getPropertyType(), prop.getFullBeanName()));
    }
    return assoc;
  }

  private void readManyToOne(ManyToOne propAnn, DeployBeanProperty prop) {

    DeployBeanPropertyAssocOne<?> beanProp = (DeployBeanPropertyAssocOne<?>) prop;

    setCascadeTypes(propAnn.cascade(), beanProp.getCascadeInfo());

    beanProp.setBeanTable(beanTable(beanProp));
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
      prop.setOrphanRemoval(propAnn.orphanRemoval());
    }

    setCascadeTypes(propAnn.cascade(), prop.getCascadeInfo());
    prop.setBeanTable(beanTable(prop));
  }

  private void readPrimaryKeyJoin(PrimaryKeyJoinColumn primaryKeyJoin, DeployBeanPropertyAssocOne<?> prop) {

    if (!prop.isOneToOne()) {
      throw new IllegalStateException("Expecting property " + prop.getFullBeanName() + " with PrimaryKeyJoinColumn to be a OneToOne?");
    }
    prop.setPrimaryKeyJoin(true);

    if (!primaryKeyJoin.name().isEmpty()) {
      log.warn("Automatically determining join columns and ignoring PrimaryKeyJoinColumn.name {} on {}", primaryKeyJoin.name(), prop.getFullBeanName());
    }
    if (!primaryKeyJoin.referencedColumnName().isEmpty()) {
      log.warn("Automatically determining join columns and Ignoring PrimaryKeyJoinColumn.referencedColumnName {} on {}", primaryKeyJoin.referencedColumnName(), prop.getFullBeanName());
    }

    BeanTable baseBeanTable = factory.getBeanTable(info.getDescriptor().getBeanType());

    String localPrimaryKey = baseBeanTable.getIdColumn();
    String foreignColumn = beanTable(prop).getIdColumn();

    prop.getTableJoin().addJoinColumn(new DeployTableJoinColumn(localPrimaryKey, foreignColumn, false, false));
  }

  private void readEmbedded(DeployBeanPropertyAssocOne<?> prop, Embedded embedded) {

    if (descriptor.isDocStoreOnly() && prop.getDocStoreDoc() == null) {
      prop.setDocStoreEmbedded("");
    }
    prop.setEmbedded();
    prop.setDbInsertable(true);
    prop.setDbUpdateable(true);
    try {
      prop.setColumnPrefix(embedded.prefix());
    } catch (NoSuchMethodError e) {
      // using standard JPA API without prefix option, maybe in EE container
    }

    readEmbeddedAttributeOverrides(prop);
  }

}
