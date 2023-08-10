package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.DbForeignKey;
import io.ebean.annotation.FetchPreference;
import io.ebean.annotation.TenantId;
import io.ebean.annotation.Where;
import io.ebean.config.NamingConvention;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.BeanTable;
import io.ebeaninternal.server.deploy.PropertyForeignKey;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import io.ebeaninternal.server.query.SqlJoinType;

import jakarta.persistence.*;

import static java.lang.System.Logger.Level.INFO;

/**
 * Read the deployment annotations for Associated One beans.
 */
final class AnnotationAssocOnes extends AnnotationAssoc {

  /**
   * Create with the deploy Info.
   */
  AnnotationAssocOnes(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig, BeanDescriptorManager factory) {
    super(info, readConfig, factory);
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
    Column column = prop.getMetaAnnotation(Column.class);
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

    Where where = prop.getMetaAnnotationWhere(platform);
    if (where != null) {
      // not expecting this to be used on assoc one properties
      prop.setExtraWhere(processFormula(where.clause()));
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
    if (readConfig.isValidationNotNull(prop)) {
      // overrides optional attribute of ManyToOne etc
      prop.setNullable(false);
      prop.getTableJoin().setType(SqlJoinType.INNER);
    }

    // check for manually defined joins
    BeanTable beanTable = prop.getBeanTable();
    for (JoinColumn joinColumn : annotationJoinColumns(prop)) {
      setFromJoinColumn(prop, beanTable, joinColumn);
      checkForNoConstraint(prop, joinColumn);
    }

    JoinTable joinTable = get(prop, JoinTable.class);
    if (joinTable != null) {
      for (JoinColumn joinColumn : joinTable.joinColumns()) {
        setFromJoinColumn(prop, beanTable, joinColumn);
      }
    }

    prop.setJoinType(prop.isNullable() || prop.getForeignKey() != null && prop.getForeignKey().isNoConstraint());

    if (!prop.getTableJoin().hasJoinColumns() && beanTable != null) {

      //noinspection StatementWithEmptyBody
      if (prop.getMappedBy() != null) {
        // the join is derived by reversing the join information
        // from the mapped by property.
        // Refer BeanDescriptorManager.readEntityRelationships()

      } else {
        // use naming convention to define join.
        NamingConvention nc = factory.namingConvention();

        String fkeyPrefix = null;
        if (nc.isUseForeignKeyPrefix()) {
          fkeyPrefix = nc.getColumnFromProperty(beanType, prop.getName());
        }

        beanTable.createJoinColumn(fkeyPrefix, prop.getTableJoin(), true, prop.getSqlFormulaSelect());
      }
    }
  }

  private void setFromJoinColumn(DeployBeanPropertyAssocOne<?> prop, BeanTable beanTable, JoinColumn joinColumn) {
    if (beanTable == null) {
      throw new IllegalStateException("Looks like a missing @ManyToOne or @OneToOne on property " + prop + " - no related 'BeanTable'");
    }
    prop.getTableJoin().addJoinColumn(util, false, joinColumn, beanTable);
    if (!joinColumn.updatable()) {
      prop.setDbUpdateable(false);
    }
    if (!joinColumn.nullable()) {
      prop.setNullable(false);
    }
  }

  private void checkForNoConstraint(DeployBeanPropertyAssocOne<?> prop, JoinColumn joinColumn) {
    try {
      ForeignKey foreignKey = joinColumn.foreignKey();
      if (foreignKey.value() == ConstraintMode.NO_CONSTRAINT) {
        prop.setForeignKey(new PropertyForeignKey());
      }
    } catch (NoSuchMethodError e) {
      // support old JPA API
    }
  }

  private void readManyToOne(ManyToOne propAnn, DeployBeanPropertyAssocOne<?> beanProp) {
    setCascadeTypes(propAnn.cascade(), beanProp.getCascadeInfo());
    setTargetType(propAnn.targetEntity(), beanProp);
    setBeanTable(beanProp);
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
    if (readOrphanRemoval(propAnn)) {
      prop.setOrphanRemoval();
    }
    if (!"".equals(propAnn.mappedBy())) {
      prop.setOneToOneExported();
    }

    setCascadeTypes(propAnn.cascade(), prop.getCascadeInfo());
    setTargetType(propAnn.targetEntity(), prop);
    setBeanTable(prop);
  }

  private boolean readOrphanRemoval(OneToOne property) {
    try {
      return property.orphanRemoval();
    } catch (NoSuchMethodError e) {
      // Support old JPA API
      return false;
    }
  }

  private void readPrimaryKeyJoin(PrimaryKeyJoinColumn primaryKeyJoin, DeployBeanPropertyAssocOne<?> prop) {
    if (!prop.isOneToOne()) {
      throw new IllegalStateException("Expecting property " + prop + " with PrimaryKeyJoinColumn to be a OneToOne?");
    }
    prop.setPrimaryKeyJoin(true);

    if (!primaryKeyJoin.name().isEmpty()) {
      CoreLog.internal.log(INFO, "Automatically determining join columns for @PrimaryKeyJoinColumn - ignoring PrimaryKeyJoinColumn.name attribute [{0}] on {1}", primaryKeyJoin.name(), prop);
    }
    if (!primaryKeyJoin.referencedColumnName().isEmpty()) {
      CoreLog.internal.log(INFO, "Automatically determining join columns for @PrimaryKeyJoinColumn - Ignoring PrimaryKeyJoinColumn.referencedColumnName attribute [{0}] on {1}", primaryKeyJoin.referencedColumnName(), prop);
    }
    BeanTable baseBeanTable = factory.beanTable(info.getDescriptor().getBeanType());
    String localPrimaryKey = baseBeanTable.getIdColumn();
    String foreignColumn = getBeanTable(prop).getIdColumn();
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
