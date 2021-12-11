package io.ebeaninternal.server.deploy.meta;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.deploy.*;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import io.ebeaninternal.server.properties.BeanPropertySetter;
import io.ebeaninternal.server.type.ScalarTypeString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Helper object to classify BeanProperties into appropriate lists.
 */
public final class DeployBeanPropertyLists {

  private static final NoopSetter NOOP_SETTER = new NoopSetter();

  private BeanProperty versionProperty;
  private BeanProperty unmappedJson;
  private BeanProperty draft;
  private BeanProperty draftDirty;
  private BeanProperty tenant;
  private final BeanDescriptor<?> desc;
  private final LinkedHashMap<String, BeanProperty> propertyMap;
  private BeanProperty id;

  private final List<BeanProperty> local = new ArrayList<>();
  private final List<BeanProperty> mutable = new ArrayList<>();
  private final List<BeanPropertyAssocMany<?>> manys = new ArrayList<>();
  private final List<BeanProperty> nonManys = new ArrayList<>();
  private final List<BeanProperty> aggs = new ArrayList<>();
  private final List<BeanPropertyAssocOne<?>> ones = new ArrayList<>();
  private final List<BeanPropertyAssocOne<?>> onesImported = new ArrayList<>();
  private final List<BeanPropertyAssocOne<?>> embedded = new ArrayList<>();
  private final List<BeanProperty> baseScalar = new ArrayList<>();
  private final List<BeanProperty> transients = new ArrayList<>();
  private final List<BeanProperty> nonTransients = new ArrayList<>();
  private final BeanPropertyAssocOne<?> unidirectional;
  private final BeanProperty orderColumn;

  public DeployBeanPropertyLists(BeanDescriptorMap owner, BeanDescriptor<?> desc, DeployBeanDescriptor<?> deploy) {
    this.desc = desc;

    DeployBeanPropertyAssocOne<?> deployId = deploy.getIdClassProperty();
    if (deployId != null) {
      this.id = new BeanPropertyIdClass(owner, desc, deployId);
      setImportedPrimaryKeysFor(deploy, deployId);
    } else {
      setImportedPrimaryKeys(deploy);
    }

    DeployBeanProperty deployOrderColumn = deploy.getOrderColumn();
    this.orderColumn = deployOrderColumn != null ? new BeanPropertyOrderColumn(desc, deployOrderColumn) : null;

    DeployBeanPropertyAssocOne<?> deployUnidirectional = deploy.getUnidirectional();
    this.unidirectional = deployUnidirectional == null ? null : new BeanPropertyAssocOne<>(owner, desc, deployUnidirectional);

    this.propertyMap = new LinkedHashMap<>();

    // see if there is a discriminator property we should add
    String discriminatorColumn = null;
    BeanProperty discProperty = null;

    InheritInfo inheritInfo = deploy.getInheritInfo();
    if (inheritInfo != null) {
      // Create a BeanProperty for the discriminator column to support
      // using RawSql queries with inheritance
      discriminatorColumn = inheritInfo.getDiscriminatorColumn();
      DeployBeanProperty discDeployProp = new DeployBeanProperty(deploy, String.class, ScalarTypeString.INSTANCE, null);
      discDeployProp.setDiscriminator();
      discDeployProp.setName(discriminatorColumn);
      discDeployProp.setDbColumn(discriminatorColumn);
      discDeployProp.setSetter(NOOP_SETTER);

      // only register it in the propertyMap. This might not be used if
      // an explicit property is mapped to the discriminator on the bean
      discProperty = new BeanProperty(desc, discDeployProp);
    }

    for (DeployBeanProperty prop : deploy.propertiesAll()) {
      if (discriminatorColumn != null && discriminatorColumn.equals(prop.getDbColumn())) {
        // we have an explicit property mapped to the discriminator column
        prop.setDiscriminator();
        discProperty = null;
      }
      BeanProperty beanProp = createBeanProperty(owner, prop);
      propertyMap.put(beanProp.name(), beanProp);
    }

    int order = 0;
    for (BeanProperty prop : propertyMap.values()) {
      prop.setDeployOrder(order++);
      allocateToList(prop);
    }

    if (orderColumn != null) {
      orderColumn.setDeployOrder(order);
      allocateToList(orderColumn);
      propertyMap.put(orderColumn.name(), orderColumn);
    }

    if (discProperty != null) {
      // put the discriminator property into the property map only
      // (after the real properties have been organised into their lists)
      propertyMap.put(discProperty.name(), discProperty);
    }
  }

  /**
   * Find and set imported primary keys.
   * <p>
   * This is where @ManyToOne properties maps to a PFK (Primary and foreign key).
   * Perform the match by naming convention on property name and db column.
   * </p>
   */
  private void setImportedPrimaryKeys(DeployBeanDescriptor<?> deploy) {
    DeployBeanProperty id = deploy.idProperty();
    if (id instanceof DeployBeanPropertyAssocOne<?>) {
      setImportedPrimaryKeysFor(deploy, (DeployBeanPropertyAssocOne<?>) id);
    }
  }

  private void setImportedPrimaryKeysFor(DeployBeanDescriptor<?> deploy, DeployBeanPropertyAssocOne<?> id) {
    for (DeployBeanProperty prop : id.getTargetDeploy().properties()) {
      DeployBeanProperty match = findImported(deploy, prop);
      if (match != null) {
        match.setImportedPrimaryKeyColumn(prop);
      }
    }
  }

  private DeployBeanProperty findImported(DeployBeanDescriptor<?> deploy, DeployBeanProperty embeddedScalar) {
    // the logical name and db column we are looking for a match on
    String name = embeddedScalar.getName();
    String dbColumn = embeddedScalar.getDbColumn();

    DeployBeanProperty match = deploy.getBeanProperty(name);
    if (match != null) {
      return match;
    }
    // could look to match more by dbColumn

    for (DeployBeanPropertyAssocOne<?> assocOne : deploy.propertiesAssocOne()) {
      if (name.equals(assocOne.getName()) || (dbColumn != null && dbColumn.equals(assocOne.getDbColumn()))) {
        return assocOne;
      }
    }
    return null;
  }

  /**
   * Return the unidirectional.
   */
  public BeanPropertyAssocOne<?> getUnidirectional() {
    return unidirectional;
  }

  /**
   * Return the order column property.
   */
  public BeanProperty getOrderColumn() {
    return orderColumn;
  }

  /**
   * Allocate the property to a list.
   */
  private void allocateToList(BeanProperty prop) {
    if (prop.isTransient()) {
      transients.add(prop);
      if (prop.isDraft()) {
        draft = prop;
      }
      if (prop.isUnmappedJson()) {
        unmappedJson = prop;
      }
      return;
    }
    if (prop.isId()) {
      if (id != null) {
        throw new IllegalStateException("More that one @Id property on " + desc.fullName() + " ?");
      }
      id = prop;
      return;
    }
    nonTransients.add(prop);
    if (prop.isMutableScalarType()) {
      mutable.add(prop);
    }

    if (desc.inheritInfo() != null && prop.isLocal()) {
      local.add(prop);
    }

    if (prop instanceof BeanPropertyAssocMany<?>) {
      manys.add((BeanPropertyAssocMany<?>) prop);

    } else {
      nonManys.add(prop);
      if (prop.isAggregation()) {
        aggs.add(prop);
      }
      if (prop.isTenantId()) {
        tenant = prop;
      }
      if (prop instanceof BeanPropertyAssocOne<?>) {
        BeanPropertyAssocOne<?> assocOne = (BeanPropertyAssocOne<?>) prop;
        if (prop.isEmbedded()) {
          embedded.add(assocOne);
        } else {
          ones.add(assocOne);
          if (!assocOne.isOneToOneExported()) {
            onesImported.add(assocOne);
          }
        }
      } else {
        // its a "base" property...
        if (prop.isVersion()) {
          if (versionProperty == null) {
            versionProperty = prop;
          } else {
            CoreLog.internal.warn("Multiple @Version properties - property " + prop.fullName() + " not treated as a version property");
          }
        } else if (prop.isDraftDirty()) {
          draftDirty = prop;
        }
        if (!prop.isAggregation()) {
          baseScalar.add(prop);
        }
      }
    }
  }

  public LinkedHashMap<String, BeanProperty> getPropertyMap() {
    return propertyMap;
  }

  /**
   * Return the base scalar properties (excludes Id and secondary table
   * properties).
   */
  public BeanProperty[] getBaseScalar() {
    return baseScalar.toArray(new BeanProperty[0]);
  }

  public BeanProperty getId() {
    return id;
  }

  public BeanProperty[] getNonTransients() {
    return nonTransients.toArray(new BeanProperty[0]);
  }

  public BeanProperty[] getTransients() {
    return transients.toArray(new BeanProperty[0]);
  }

  public BeanProperty getVersionProperty() {
    return versionProperty;
  }

  public BeanProperty[] getLocal() {
    return local.toArray(new BeanProperty[0]);
  }

  public BeanProperty[] getMutable() {
    return mutable.toArray(new BeanProperty[0]);
  }

  public BeanPropertyAssocOne<?>[] getEmbedded() {
    return embedded.toArray(new BeanPropertyAssocOne[0]);
  }

  public BeanPropertyAssocOne<?>[] getOneImported() {
    return onesImported.toArray(new BeanPropertyAssocOne[0]);
  }

  public BeanPropertyAssocOne<?>[] getOnes() {
    return ones.toArray(new BeanPropertyAssocOne[0]);
  }

  public BeanPropertyAssocOne<?>[] getOneExportedSave() {
    return getOne(false, Mode.Save);
  }

  public BeanPropertyAssocOne<?>[] getOneExportedDelete() {
    return getOne(false, Mode.Delete);
  }

  public BeanPropertyAssocOne<?>[] getOneImportedSave() {
    return getOne(true, Mode.Save);
  }

  public BeanPropertyAssocOne<?>[] getOneImportedDelete() {
    return getOne(true, Mode.Delete);
  }

  public BeanProperty[] getNonMany() {
    return nonManys.toArray(new BeanProperty[0]);
  }

  public BeanProperty[] getAggregates() {
    return aggs.toArray(new BeanProperty[0]);
  }

  public BeanPropertyAssocMany<?>[] getMany() {
    return manys.toArray(new BeanPropertyAssocMany[0]);
  }

  public BeanPropertyAssocMany<?>[] getManySave() {
    return getMany(Mode.Save);
  }

  public BeanPropertyAssocMany<?>[] getManyDelete() {
    return getMany(Mode.Delete);
  }

  public BeanPropertyAssocMany<?>[] getManyToMany() {
    return getMany2Many();
  }

  public BeanProperty getDraftDirty() {
    return draftDirty;
  }

  public BeanProperty getUnmappedJson() {
    return unmappedJson;
  }

  public BeanProperty getDraft() {
    return draft;
  }

  public BeanProperty getSoftDeleteProperty() {
    for (BeanProperty prop : nonManys) {
      if (prop.isSoftDelete()) {
        return prop;
      }
    }
    return null;
  }

  public BeanProperty getTenant() {
    return tenant;
  }

  /**
   * Return the properties set via generated values on insert.
   */
  public BeanProperty[] getGeneratedInsert() {
    List<BeanProperty> list = new ArrayList<>();
    for (BeanProperty prop : nonTransients) {
      GeneratedProperty gen = prop.generatedProperty();
      if (gen != null && gen.includeInInsert()) {
        list.add(prop);
      }
    }
    return list.toArray(new BeanProperty[0]);
  }

  /**
   * Return the properties set via generated values on update.
   */
  public BeanProperty[] getGeneratedUpdate() {
    List<BeanProperty> list = new ArrayList<>();
    for (BeanProperty prop : nonTransients) {
      GeneratedProperty gen = prop.generatedProperty();
      if (gen != null && gen.includeInUpdate()) {
        list.add(prop);
      }
    }
    return list.toArray(new BeanProperty[0]);
  }

  /**
   * Mode used to determine which BeanPropertyAssoc to include.
   */
  private enum Mode {
    Save, Delete
  }

  private BeanPropertyAssocOne<?>[] getOne(boolean imported, Mode mode) {
    ArrayList<BeanPropertyAssocOne<?>> list = new ArrayList<>();
    for (BeanPropertyAssocOne<?> prop : ones) {
      if (imported != prop.isOneToOneExported()) {
        switch (mode) {
          case Save:
            if (prop.cascadeInfo().isSave()) {
              list.add(prop);
            }
            break;
          case Delete:
            if (prop.cascadeInfo().isDelete()) {
              list.add(prop);
            }
            break;
          default:
            break;
        }
      }
    }
    return list.toArray(new BeanPropertyAssocOne<?>[0]);
  }

  private BeanPropertyAssocMany<?>[] getMany2Many() {
    ArrayList<BeanPropertyAssocMany<?>> list = new ArrayList<>();
    for (BeanPropertyAssocMany<?> prop : manys) {
      if (prop.isManyToMany()) {
        list.add(prop);
      }
    }
    return list.toArray(new BeanPropertyAssocMany<?>[0]);
  }

  private BeanPropertyAssocMany<?>[] getMany(Mode mode) {
    ArrayList<BeanPropertyAssocMany<?>> list = new ArrayList<>();
    for (BeanPropertyAssocMany<?> prop : manys) {
      switch (mode) {
        case Save:
          if (prop.isIncludeCascadeSave()) {
            list.add(prop);
          }
          break;
        case Delete:
          if (prop.isIncludeCascadeDelete()) {
            list.add(prop);
          }
          break;
        default:
          break;
      }
    }
    return list.toArray(new BeanPropertyAssocMany<?>[0]);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private BeanProperty createBeanProperty(BeanDescriptorMap owner, DeployBeanProperty deployProp) {
    if (deployProp instanceof DeployBeanPropertyAssocOne) {
      return new BeanPropertyAssocOne(owner, desc, (DeployBeanPropertyAssocOne) deployProp);
    }
    if (deployProp instanceof DeployBeanPropertySimpleCollection<?>) {
      return new BeanPropertySimpleCollection(desc, (DeployBeanPropertySimpleCollection) deployProp);
    }
    if (deployProp instanceof DeployBeanPropertyAssocMany) {
      return new BeanPropertyAssocMany(desc, (DeployBeanPropertyAssocMany) deployProp);
    }
    if (deployProp.isJsonMapper()) {
      return new BeanPropertyJsonMapper(desc, deployProp);
    }
    if (deployProp.isJsonType()) {
      return new BeanPropertyJsonBasic(desc, deployProp);
    }
    return new BeanProperty(desc, deployProp);
  }

  private static class NoopSetter implements BeanPropertySetter {

    @Override
    public void set(EntityBean bean, Object value) {
      // do nothing
    }

    @Override
    public void setIntercept(EntityBean bean, Object value) {
      // do nothing
    }
  }
}
