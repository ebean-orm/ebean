package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.DbForeignKey;
import io.ebean.annotation.FetchPreference;
import io.ebean.annotation.HistoryExclude;
import io.ebean.annotation.Where;
import io.ebean.bean.BeanCollection.ModifyListenMode;
import io.ebean.config.NamingConvention;
import io.ebean.config.TableName;
import io.ebean.core.type.ScalarType;
import io.ebean.util.CamelCaseHelper;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanTable;
import io.ebeaninternal.server.deploy.PropertyForeignKey;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.meta.DeployOrderColumn;
import io.ebeaninternal.server.deploy.meta.DeployTableJoin;
import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import io.ebeaninternal.server.query.SqlJoinType;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EnumType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import java.util.Set;

/**
 * Read the deployment annotation for Assoc Many beans.
 */
final class AnnotationAssocManys extends AnnotationAssoc {

  AnnotationAssocManys(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig, BeanDescriptorManager factory) {
    super(info, readConfig, factory);
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

  private boolean readOrphanRemoval(OneToMany property) {
    try {
      return property.orphanRemoval();
    } catch (NoSuchMethodError e) {
      // Support old JPA API
      return false;
    }
  }

  private void read(DeployBeanPropertyAssocMany<?> prop) {
    OneToMany oneToMany = get(prop, OneToMany.class);
    if (oneToMany != null) {
      readToOne(oneToMany, prop);
      if (readOrphanRemoval(oneToMany)) {
        prop.setOrphanRemoval();
        prop.setModifyListenMode(ModifyListenMode.REMOVALS);
        prop.getCascadeInfo().setDelete(true);
      }
      OrderColumn orderColumn = get(prop, OrderColumn.class);
      if (orderColumn != null) {
        // need to cascade as we set the order on cascade
        prop.setOrderColumn(new DeployOrderColumn(orderColumn));
        prop.setFetchOrderBy(DeployOrderColumn.LOGICAL_NAME);
        prop.getCascadeInfo().setType(CascadeType.ALL);
        prop.setModifyListenMode(ModifyListenMode.ALL);
      }
    }
    ManyToMany manyToMany = get(prop, ManyToMany.class);
    if (manyToMany != null) {
      readToMany(manyToMany, prop);
    }
    ElementCollection elementCollection = get(prop, ElementCollection.class);
    if (elementCollection != null) {
      readElementCollection(prop, elementCollection);
    }

    // for ManyToMany typically to disable foreign keys from intersection table
    DbForeignKey dbForeignKey = get(prop, DbForeignKey.class);
    if (dbForeignKey != null){
      prop.setForeignKey(new PropertyForeignKey(dbForeignKey));
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
    Where where = prop.getMetaAnnotationWhere(platform);
    if (where != null) {
      prop.setExtraWhere(processFormula(where.clause()));
    }
    FetchPreference fetchPreference = get(prop, FetchPreference.class);
    if (fetchPreference != null) {
      prop.setFetchPreference(fetchPreference.value());
    }
    // check for manually defined joins
    BeanTable beanTable = prop.getBeanTable();

    Set<JoinColumn> joinColumns = annotationJoinColumns(prop);
    if (!joinColumns.isEmpty()) {
      prop.getTableJoin().addJoinColumn(util, true, joinColumns, beanTable);
    }

    JoinTable joinTable = get(prop, JoinTable.class);
    if (joinTable != null) {
      if (prop.isManyToMany()) {
        readJoinTable(joinTable, prop);
      } else {
        // OneToMany with @JoinTable
        prop.setO2mJoinTable();
        readJoinTable(joinTable, prop);
        manyToManyDefaultJoins(prop);
      }
    } else if (prop.isManyToMany()) {
      checkSelfManyToMany(prop);
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
      NamingConvention nc = factory.namingConvention();
      String fkeyPrefix = null;
      if (nc.isUseForeignKeyPrefix()) {
        fkeyPrefix = nc.getColumnFromProperty(descriptor.getBeanType(), descriptor.getName());
      }
      // Use the owning bean table to define the join
      BeanTable owningBeanTable = factory.beanTable(descriptor.getBeanType());
      owningBeanTable.createJoinColumn(fkeyPrefix, prop.getTableJoin(), false, prop.getSqlFormulaSelect());
    }
  }

  private void checkSelfManyToMany(DeployBeanPropertyAssocMany<?> prop) {
    if (prop.getTargetType().equals(descriptor.getBeanType())) {
      throw new IllegalStateException("@ManyToMany mapping for " + prop.getFullBeanName() + " requires explicit @JoinTable with joinColumns & inverseJoinColumns. Refer issue #2157");
    }
  }

  @SuppressWarnings("unchecked")
  private void readElementCollection(DeployBeanPropertyAssocMany<?> prop, ElementCollection elementCollection) {
    prop.setElementCollection();
    if (!elementCollection.targetClass().equals(void.class)) {
      prop.setTargetType(elementCollection.targetClass());
    }
    Column column = prop.getMetaAnnotation(Column.class);
    if (column != null) {
      prop.setDbColumn(column.name());
      prop.setDbLength(column.length());
      prop.setDbScale(column.scale());
    }

    CollectionTable collectionTable = get(prop, CollectionTable.class);
    String fullTableName = getFullTableName(collectionTable);
    if (fullTableName == null) {
      fullTableName = descriptor.getBaseTable()+"_"+ CamelCaseHelper.toUnderscoreFromCamel(prop.getName());
    }

    BeanTable localTable = factory.beanTable(descriptor.getBeanType());
    if (collectionTable != null) {
      prop.getTableJoin().addJoinColumn(util, true, collectionTable.joinColumns(), localTable);
    }
    if (!prop.getTableJoin().hasJoinColumns()) {
      BeanProperty localId = localTable.getIdProperty();
      if (localId != null) {
        // add foreign key based on convention
        String fkColName = namingConvention.getForeignKey(descriptor.getBaseTable(), localId.name());
        prop.getTableJoin().addJoinColumn(new DeployTableJoinColumn(localId.dbColumn(), fkColName));
      }
    }

    BeanTable beanTable = factory.createCollectionBeanTable(fullTableName, prop.getTargetType());
    prop.setBeanTable(beanTable);

    Class<?> elementType = prop.getTargetType();

    DeployBeanDescriptor<?> elementDescriptor = factory.createDeployDescriptor(elementType);
    elementDescriptor.setBaseTable(new TableName(fullTableName), readConfig.getAsOfViewSuffix(), readConfig.getVersionsBetweenSuffix());

    int sortOrder = 0;
    if (!prop.getManyType().isMap()) {
      elementDescriptor.setProperties(new String[]{"value"});
    } else {
      elementDescriptor.setProperties(new String[]{"key", "value"});
      String dbKeyColumn = "mkey";
      MapKeyColumn mapKeyColumn = get(prop, MapKeyColumn.class);
      if (mapKeyColumn != null) {
        dbKeyColumn = mapKeyColumn.name();
      }

      ScalarType<?> keyScalarType = util.getTypeManager().getScalarType(prop.getMapKeyType());

      DeployBeanProperty keyProp = new DeployBeanProperty(elementDescriptor, elementType, keyScalarType, null);
      setElementProperty(keyProp, "key", dbKeyColumn, sortOrder++);
      elementDescriptor.addBeanProperty(keyProp);
      if (mapKeyColumn != null) {
        keyProp.setDbLength(mapKeyColumn.length());
        keyProp.setDbScale(mapKeyColumn.scale());
        keyProp.setUnique(mapKeyColumn.unique());
      }
    }

    ScalarType<?> valueScalarType = util.getTypeManager().getScalarType(elementType);
    if (valueScalarType == null && elementType.isEnum()) {
      Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>)elementType;
      valueScalarType = util.getTypeManager().createEnumScalarType(enumClass, EnumType.STRING);
    }

    boolean scalar = true;
    if (valueScalarType == null) {
      // embedded value type
      scalar = false;
      DeployBeanPropertyAssocOne valueProp = new DeployBeanPropertyAssocOne<>(elementDescriptor, elementType);
      valueProp.setName("value");
      valueProp.setEmbedded();
      valueProp.setElementProperty();
      valueProp.setSortOrder(sortOrder++);
      elementDescriptor.addBeanProperty(valueProp);

    } else {
      // scalar value type
      DeployBeanProperty valueProp = new DeployBeanProperty(elementDescriptor, elementType, valueScalarType, null);
      setElementProperty(valueProp, "value", prop.getDbColumn(), sortOrder++);
      if (column != null) {
        valueProp.setDbLength(column.length());
        valueProp.setDbScale(column.scale());
      }
      Lob lob = get(prop, Lob.class);
      if (lob != null) {
        util.setLobType(valueProp);
      }
      elementDescriptor.addBeanProperty(valueProp);
    }

    elementDescriptor.setName(prop.getFullBeanName());

    factory.createUnidirectional(elementDescriptor, prop.getOwningType(), beanTable, prop.getTableJoin());
    prop.setElementDescriptor(factory.createElementDescriptor(elementDescriptor, prop.getManyType(), scalar));
  }

  private void setElementProperty(DeployBeanProperty elementProp, String name, String dbColumn, int sortOrder) {
    if (dbColumn == null) {
      dbColumn = "value";
    }
    elementProp.setName(name);
    elementProp.setDbColumn(dbColumn);
    elementProp.setNullable(false);
    elementProp.setDbInsertable(true);
    elementProp.setDbUpdateable(true);
    elementProp.setDbRead(true);
    elementProp.setSortOrder(sortOrder);
    elementProp.setElementProperty();
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
    if (intTableName.isEmpty()) {
      BeanTable localTable = factory.beanTable(descriptor.getBeanType());
      BeanTable otherTable = factory.beanTable(prop.getTargetType());
      intTableName = getM2MJoinTableName(localTable, otherTable);
    }

    // set the intersection table
    DeployTableJoin intJoin = new DeployTableJoin();
    intJoin.setTable(intTableName);

    // add the source to intersection join columns
    intJoin.addJoinColumn(util, true, joinTable.joinColumns(), prop.getBeanTable());

    // set the intersection to dest table join columns
    DeployTableJoin destJoin = prop.getTableJoin();
    destJoin.addJoinColumn(util, false, joinTable.inverseJoinColumns(), prop.getBeanTable());

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
    return append(joinTable.catalog(), joinTable.schema(), joinTable.name());
  }

  /**
   * Return the full table name
   */
  private String getFullTableName(CollectionTable collectionTable) {
    if (collectionTable == null || collectionTable.name().isEmpty()) {
      return null;
    }
    return append(collectionTable.catalog(), collectionTable.schema(), collectionTable.name());
  }

  /**
   * Return the full table name taking into account quoted identifiers.
   */
  private String append(String catalog, String schema, String name) {
    return namingConvention.getTableName(catalog, schema, name);
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

    BeanTable localTable = factory.beanTable(descriptor.getBeanType());
    BeanTable otherTable = factory.beanTable(prop.getTargetType());

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
      BeanProperty localId = localTable.getIdProperty();
      if (localId != null) {
        // add the source to intersection join columns
        String fkCol = namingConvention.deriveM2MColumn(localTableName, localId.dbColumn());
        intJoin.addJoinColumn(new DeployTableJoinColumn(localId.dbColumn(), fkCol));
      }
    }

    if (!destJoin.hasJoinColumns()) {
      // define inverse foreign key columns
      BeanProperty otherId = otherTable.getIdProperty();
      if (otherId != null) {
        // set the intersection to dest table join columns
        String fkCol = namingConvention.deriveM2MColumn(otherTableName, otherId.dbColumn());
        destJoin.addJoinColumn(new DeployTableJoinColumn(fkCol, otherId.dbColumn()));
      }
    }

    // reverse join from dest back to intersection
    DeployTableJoin inverseDest = destJoin.createInverse(intTableName);
    prop.setInverseJoin(inverseDest);
  }

  private void readToMany(ManyToMany propAnn, DeployBeanPropertyAssocMany<?> manyProp) {
    manyProp.setMappedBy(propAnn.mappedBy());
    manyProp.setFetchType(propAnn.fetch());
    setCascadeTypes(propAnn.cascade(), manyProp.getCascadeInfo());
    setTargetType(propAnn.targetEntity(), manyProp);
    setBeanTable(manyProp);
    manyProp.setManyToMany();
    manyProp.setModifyListenMode(ModifyListenMode.ALL);
    manyProp.getTableJoin().setType(SqlJoinType.OUTER);
  }

  private void readToOne(OneToMany propAnn, DeployBeanPropertyAssocMany<?> manyProp) {
    manyProp.setMappedBy(propAnn.mappedBy());
    manyProp.setFetchType(propAnn.fetch());
    setCascadeTypes(propAnn.cascade(), manyProp.getCascadeInfo());
    setTargetType(propAnn.targetEntity(), manyProp);
    setBeanTable(manyProp);
    manyProp.getTableJoin().setType(SqlJoinType.OUTER);
  }

  private String getM2MJoinTableName(BeanTable lhsTable, BeanTable rhsTable) {
    TableName lhs = new TableName(lhsTable.getBaseTable());
    TableName rhs = new TableName(rhsTable.getBaseTable());
    TableName joinTable = namingConvention.getM2MJoinTableName(lhs, rhs);
    return joinTable.getQualifiedName();
  }
}
