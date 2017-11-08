package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.FetchPreference;
import io.ebean.annotation.HistoryExclude;
import io.ebean.annotation.PrivateOwned;
import io.ebean.annotation.Where;
import io.ebean.bean.BeanCollection.ModifyListenMode;
import io.ebean.config.NamingConvention;
import io.ebean.config.TableName;
import io.ebean.util.StringHelper;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanTable;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.meta.DeployTableJoin;
import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import io.ebeaninternal.server.query.SqlJoinType;

import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.Set;

/**
 * Read the deployment annotation for Assoc Many beans.
 */
class AnnotationAssocManys extends AnnotationParser {

  private final BeanDescriptorManager factory;

  /**
   * Create with the DeployInfo.
   */
  AnnotationAssocManys(DeployBeanInfo<?> info, boolean javaxValidationAnnotations, BeanDescriptorManager factory) {
    super(info, javaxValidationAnnotations);
    this.factory = factory;
  }

  /**
   * Parse the annotations.
   */
  @Override
  public void parse() {
    for (DeployBeanProperty prop : descriptor.propertiesAll()) {
      if (prop instanceof DeployBeanPropertyAssocMany<?>) {
        read((DeployBeanPropertyAssocMany<?>) prop);
      }
    }
  }

  private void read(DeployBeanPropertyAssocMany<?> prop) {

    OneToMany oneToMany = get(prop, OneToMany.class);
    if (oneToMany != null) {
      readToOne(oneToMany, prop);
      if (oneToMany.orphanRemoval()) {
        prop.setModifyListenMode(ModifyListenMode.REMOVALS);
        prop.getCascadeInfo().setDelete(true);
      }
      PrivateOwned privateOwned = get(prop, PrivateOwned.class);
      if (privateOwned != null) {
        prop.setModifyListenMode(ModifyListenMode.REMOVALS);
        prop.getCascadeInfo().setDelete(privateOwned.cascadeRemove());
      }
    }
    ManyToMany manyToMany = get(prop, ManyToMany.class);
    if (manyToMany != null) {
      readToMany(manyToMany, prop);
    }

    if (get(prop, HistoryExclude.class) != null) {
      prop.setExcludedFromHistory();
    }

    OrderBy orderBy = get(prop, OrderBy.class);
    if (orderBy != null) {
      prop.setFetchOrderBy(orderBy.value());
    }

    MapKey mapKey = get(prop, MapKey.class);
    if (mapKey != null) {
      prop.setMapKey(mapKey.name());
    }

    Where where = get(prop, Where.class);
    if (where != null) {
      prop.setExtraWhere(where.clause());
    }

    FetchPreference fetchPreference = get(prop, FetchPreference.class);
    if (fetchPreference != null) {
      prop.setFetchPreference(fetchPreference.value());
    }

    // check for manually defined joins
    BeanTable beanTable = prop.getBeanTable();

    Set<JoinColumn> joinColumns = getAll(prop, JoinColumn.class);
    if (joinColumns != null) {
      prop.getTableJoin().addJoinColumn(true, joinColumns, beanTable);
    }

    JoinTable joinTable = get(prop, JoinTable.class);
    if (joinTable != null) {
      if (prop.isManyToMany()) {
        // expected this
        readJoinTable(joinTable, prop);

      } else {
        // OneToMany in theory
        prop.getTableJoin().addJoinColumn(true, joinTable.joinColumns(), beanTable);
      }
    }

    if (prop.getMappedBy() != null) {
      // the join is derived by reversing the join information
      // from the mapped by property.
      // Refer BeanDescriptorManager.readEntityRelationships()
      return;
    }

    if (prop.isManyToMany()) {
      manyToManyDefaultJoins(prop);
      return;
    }


    if (!prop.getTableJoin().hasJoinColumns() && beanTable != null) {

      // use naming convention to define join (based on the bean name for this side of relationship)
      // A unidirectional OneToMany or OneToMany with no mappedBy property

      NamingConvention nc = factory.getNamingConvention();

      String fkeyPrefix = null;
      if (nc.isUseForeignKeyPrefix()) {
        fkeyPrefix = nc.getColumnFromProperty(descriptor.getBeanType(), descriptor.getName());
      }

      // Use the owning bean table to define the join
      BeanTable owningBeanTable = factory.getBeanTable(descriptor.getBeanType());
      owningBeanTable.createJoinColumn(fkeyPrefix, prop.getTableJoin(), false, prop.getSqlFormulaSelect());
    }
  }

  /**
   * Define the joins for a ManyToMany relationship.
   * <p>
   * This includes joins to the intersection table and from the intersection table
   * to the other side of the ManyToMany.
   * </p>
   */
  private void readJoinTable(JoinTable joinTable, DeployBeanPropertyAssocMany<?> prop) {

    String intTableName = getFullTableName(joinTable);
    // set the intersection table
    DeployTableJoin intJoin = new DeployTableJoin();
    intJoin.setTable(intTableName);

    // add the source to intersection join columns
    intJoin.addJoinColumn(true, joinTable.joinColumns(), prop.getBeanTable());

    // set the intersection to dest table join columns
    DeployTableJoin destJoin = prop.getTableJoin();
    destJoin.addJoinColumn(false, joinTable.inverseJoinColumns(), prop.getBeanTable());

    intJoin.setType(SqlJoinType.OUTER);

    // reverse join from dest back to intersection
    DeployTableJoin inverseDest = destJoin.createInverse(intTableName);
    prop.setIntersectionJoin(intJoin);
    prop.setInverseJoin(inverseDest);
  }

  /**
   * Return the full table name
   */
  private String getFullTableName(JoinTable joinTable) {
    StringBuilder sb = new StringBuilder();
    if (!StringHelper.isNull(joinTable.catalog())) {
      sb.append(joinTable.catalog()).append(".");
    }
    if (!StringHelper.isNull(joinTable.schema())) {
      sb.append(joinTable.schema()).append(".");
    }
    sb.append(joinTable.name());
    return sb.toString();
  }

  /**
   * Define intersection table and foreign key columns for ManyToMany.
   * <p>
   * Some of these (maybe all) have been already defined via @JoinTable
   * and @JoinColumns etc.
   * </p>
   */
  private void manyToManyDefaultJoins(DeployBeanPropertyAssocMany<?> prop) {

    String intTableName = null;

    DeployTableJoin intJoin = prop.getIntersectionJoin();
    if (intJoin == null) {
      intJoin = new DeployTableJoin();
      prop.setIntersectionJoin(intJoin);
    } else {
      // intersection table already defined (by @JoinTable)
      intTableName = intJoin.getTable();
    }

    BeanTable localTable = factory.getBeanTable(descriptor.getBeanType());
    BeanTable otherTable = factory.getBeanTable(prop.getTargetType());

    final String localTableName = localTable.getUnqualifiedBaseTable();
    final String otherTableName = otherTable.getUnqualifiedBaseTable();

    if (intTableName == null) {
      // define intersection table name
      intTableName = getM2MJoinTableName(localTable, otherTable);

      intJoin.setTable(intTableName);
      intJoin.setType(SqlJoinType.OUTER);
    }

    DeployTableJoin destJoin = prop.getTableJoin();


    if (intJoin.hasJoinColumns() && destJoin.hasJoinColumns()) {
      // already defined the foreign key columns etc
      return;
    }
    if (!intJoin.hasJoinColumns()) {
      // define foreign key columns
      BeanProperty[] localIds = localTable.getIdProperties();
      for (BeanProperty localId : localIds) {
        // add the source to intersection join columns
        String fkCol = localTableName + "_" + localId.getDbColumn();
        intJoin.addJoinColumn(new DeployTableJoinColumn(localId.getDbColumn(), namingConvention.getColumnFromProperty(null, fkCol)));
      }
    }

    if (!destJoin.hasJoinColumns()) {
      // define inverse foreign key columns
      BeanProperty[] otherIds = otherTable.getIdProperties();
      for (BeanProperty otherId : otherIds) {
        // set the intersection to dest table join columns
        final String fkCol = otherTableName + "_" + otherId.getDbColumn();
        destJoin.addJoinColumn(new DeployTableJoinColumn(namingConvention.getColumnFromProperty(null, fkCol), otherId.getDbColumn()));
      }
    }

    // reverse join from dest back to intersection
    DeployTableJoin inverseDest = destJoin.createInverse(intTableName);
    prop.setInverseJoin(inverseDest);
  }


  private String errorMsgMissingBeanTable(Class<?> type, String from) {
    return "Error with association to [" + type + "] from [" + from + "]. Is " + type + " registered?";
  }

  private void readToMany(ManyToMany propAnn, DeployBeanPropertyAssocMany<?> manyProp) {

    manyProp.setMappedBy(propAnn.mappedBy());
    manyProp.setFetchType(propAnn.fetch());

    setCascadeTypes(propAnn.cascade(), manyProp.getCascadeInfo());

    Class<?> targetType = propAnn.targetEntity();
    if (targetType.equals(void.class)) {
      // via reflection of generics type
      targetType = manyProp.getTargetType();
    } else {
      manyProp.setTargetType(targetType);
    }

    // find the other many table (not intersection)
    BeanTable assoc = factory.getBeanTable(targetType);
    if (assoc == null) {
      String msg = errorMsgMissingBeanTable(targetType, manyProp.getFullBeanName());
      throw new RuntimeException(msg);
    }

    manyProp.setManyToMany();
    manyProp.setModifyListenMode(ModifyListenMode.ALL);
    manyProp.setBeanTable(assoc);
    manyProp.getTableJoin().setType(SqlJoinType.OUTER);
  }

  private void readToOne(OneToMany propAnn, DeployBeanPropertyAssocMany<?> manyProp) {

    manyProp.setMappedBy(propAnn.mappedBy());
    manyProp.setFetchType(propAnn.fetch());

    setCascadeTypes(propAnn.cascade(), manyProp.getCascadeInfo());

    Class<?> targetType = propAnn.targetEntity();
    if (targetType.equals(void.class)) {
      // via reflection of generics type
      targetType = manyProp.getTargetType();
    } else {
      manyProp.setTargetType(targetType);
    }

    BeanTable assoc = factory.getBeanTable(targetType);
    if (assoc == null) {
      String msg = errorMsgMissingBeanTable(targetType, manyProp.getFullBeanName());
      throw new RuntimeException(msg);
    }

    manyProp.setBeanTable(assoc);
    manyProp.getTableJoin().setType(SqlJoinType.OUTER);
  }


  private String getM2MJoinTableName(BeanTable lhsTable, BeanTable rhsTable) {

    TableName lhs = new TableName(lhsTable.getBaseTable());
    TableName rhs = new TableName(rhsTable.getBaseTable());

    TableName joinTable = namingConvention.getM2MJoinTableName(lhs, rhs);

    return joinTable.getQualifiedName();
  }
}
