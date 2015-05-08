package com.avaje.ebeaninternal.server.deploy.meta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.bean.BeanCollection.ModifyListenMode;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorMap;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;
import com.avaje.ebeaninternal.server.deploy.BeanPropertySimpleCollection;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.deploy.TableJoin;
import com.avaje.ebeaninternal.server.type.ScalarTypeString;

/**
 * Helper object to classify BeanProperties into appropriate lists.
 */
public class DeployBeanPropertyLists {

  private static final Logger logger = LoggerFactory.getLogger(DeployBeanPropertyLists.class);

  private BeanProperty versionProperty;

  private final BeanDescriptor<?> desc;

  private final LinkedHashMap<String, BeanProperty> propertyMap;

  private final ArrayList<BeanProperty> ids = new ArrayList<BeanProperty>();

  private final ArrayList<BeanProperty> local = new ArrayList<BeanProperty>();

  private final ArrayList<BeanProperty> mutable = new ArrayList<BeanProperty>();

  private final ArrayList<BeanProperty> manys = new ArrayList<BeanProperty>();
  
  private final ArrayList<BeanProperty> nonManys = new ArrayList<BeanProperty>();

  private final ArrayList<BeanProperty> ones = new ArrayList<BeanProperty>();

  private final ArrayList<BeanProperty> onesExported = new ArrayList<BeanProperty>();

  private final ArrayList<BeanProperty> onesImported = new ArrayList<BeanProperty>();

  private final ArrayList<BeanProperty> embedded = new ArrayList<BeanProperty>();

  private final ArrayList<BeanProperty> baseScalar = new ArrayList<BeanProperty>();

  private final ArrayList<BeanPropertyCompound> baseCompound = new ArrayList<BeanPropertyCompound>();

  private final ArrayList<BeanProperty> transients = new ArrayList<BeanProperty>();

  private final ArrayList<BeanProperty> nonTransients = new ArrayList<BeanProperty>();

  private final TableJoin[] tableJoins;

  private final BeanPropertyAssocOne<?> unidirectional;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public DeployBeanPropertyLists(BeanDescriptorMap owner, BeanDescriptor<?> desc, DeployBeanDescriptor<?> deploy) {
    this.desc = desc;

    DeployBeanPropertyAssocOne<?> deployUnidirectional = deploy.getUnidirectional();
    if (deployUnidirectional == null) {
      unidirectional = null;
    } else {
      unidirectional = new BeanPropertyAssocOne(owner, desc, deployUnidirectional);
    }

    this.propertyMap = new LinkedHashMap<String, BeanProperty>();

    for (DeployBeanProperty prop : deploy.propertiesAll()) {
      BeanProperty beanProp = createBeanProperty(owner, prop);
      propertyMap.put(beanProp.getName(), beanProp);
    }

    int order = 0;
    for (BeanProperty prop : propertyMap.values()) {
      prop.setDeployOrder(order++);
      allocateToList(prop);
    }

    InheritInfo inheritInfo = deploy.getInheritInfo();
    if (inheritInfo != null) {
      // Create a BeanProperty for the discriminator column to support 
      // using RawSql queries with inheritance
      String discriminatorColumn = inheritInfo.getDiscriminatorColumn();
      DeployBeanProperty discDeployProp = new DeployBeanProperty(deploy, String.class, new ScalarTypeString(), null);
      discDeployProp.setDiscriminator(true);
      discDeployProp.setName(discriminatorColumn);
      discDeployProp.setDbColumn(discriminatorColumn);
      
      // create the discriminator BeanProperty and only register it in the propertyMap
      BeanProperty dprop = new BeanProperty(owner, desc, discDeployProp);
      propertyMap.put(dprop.getName(), dprop);
    }
    
    List<DeployTableJoin> deployTableJoins = deploy.getTableJoins();
    tableJoins = new TableJoin[deployTableJoins.size()];
    for (int i = 0; i < deployTableJoins.size(); i++) {
      tableJoins[i] = new TableJoin(deployTableJoins.get(i), propertyMap);
    }

  }

  /**
   * Return the unidirectional.
   */
  public BeanPropertyAssocOne<?> getUnidirectional() {
    return unidirectional;
  }

  /**
   * Allocate the property to a list.
   */
  private void allocateToList(BeanProperty prop) {
    if (prop.isTransient()) {
      transients.add(prop);
      return;
    }
    if (prop.isId()) {
      ids.add(prop);
      return;
    } else {
      nonTransients.add(prop);
    }

    if (prop.isMutableScalarType()) {
      mutable.add(prop);
    }
    
    if (desc.getInheritInfo() != null && prop.isLocal()) {
      local.add(prop);
    }

    if (prop instanceof BeanPropertyAssocMany<?>) {
      manys.add(prop);

    } else {
      nonManys.add(prop);
      if (prop instanceof BeanPropertyAssocOne<?>) {
        if (prop.isEmbedded()) {
          embedded.add(prop);
        } else {
          ones.add(prop);
          BeanPropertyAssocOne<?> assocOne = (BeanPropertyAssocOne<?>) prop;
          if (assocOne.isOneToOneExported()) {
            onesExported.add(prop);
          } else {
            onesImported.add(prop);
          }
        }
      } else {
        // its a "base" property...
        if (prop.isVersion()) {
          if (versionProperty == null) {
            versionProperty = prop;
          } else {
            logger.warn("Multiple @Version properties - property " + prop.getFullBeanName()
                + " not treated as a version property");
          }
        }
        if (prop instanceof BeanPropertyCompound) {
          baseCompound.add((BeanPropertyCompound) prop);
        } else {
          baseScalar.add(prop);
        }
      }
    }
  }

  public LinkedHashMap<String, BeanProperty> getPropertyMap() {
    return propertyMap;
  }

  public TableJoin[] getTableJoin() {
    return tableJoins;
  }

  /**
   * Return the base scalar properties (excludes Id and secondary table
   * properties).
   */
  public BeanProperty[] getBaseScalar() {
    return (BeanProperty[]) baseScalar.toArray(new BeanProperty[baseScalar.size()]);
  }

  public BeanPropertyCompound[] getBaseCompound() {
    return (BeanPropertyCompound[]) baseCompound.toArray(new BeanPropertyCompound[baseCompound.size()]);
  }

  public BeanProperty getId() {
    if (ids.size() > 1) {
      String msg = "Issue with bean "+desc+". Ebean does not support multiple @Id properties. You need to convert to using an @EmbeddedId."
          +" Please email the ebean google group if you need further clarification.";
      throw new IllegalStateException(msg);
    }
    if (ids.isEmpty()) {
      return null;
    }
    return ids.get(0);
  }

  public BeanProperty[] getNonTransients() {
    return (BeanProperty[]) nonTransients.toArray(new BeanProperty[nonTransients.size()]);
  }

  public BeanProperty[] getTransients() {
    return (BeanProperty[]) transients.toArray(new BeanProperty[transients.size()]);
  }

  public BeanProperty getVersionProperty() {
    return versionProperty;
  }

  public BeanProperty[] getLocal() {
    return (BeanProperty[]) local.toArray(new BeanProperty[local.size()]);
  }

  public BeanProperty[] getMutable() {
    return (BeanProperty[]) mutable.toArray(new BeanProperty[mutable.size()]);
  }

  public BeanPropertyAssocOne<?>[] getEmbedded() {
    return (BeanPropertyAssocOne[]) embedded.toArray(new BeanPropertyAssocOne[embedded.size()]);
  }

  public BeanPropertyAssocOne<?>[] getOneExported() {
    return (BeanPropertyAssocOne[]) onesExported.toArray(new BeanPropertyAssocOne[onesExported.size()]);
  }

  public BeanPropertyAssocOne<?>[] getOneImported() {
    return (BeanPropertyAssocOne[]) onesImported.toArray(new BeanPropertyAssocOne[onesImported.size()]);
  }

  public BeanPropertyAssocOne<?>[] getOnes() {
    return (BeanPropertyAssocOne[]) ones.toArray(new BeanPropertyAssocOne[ones.size()]);
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
    return (BeanProperty[]) nonManys.toArray(new BeanProperty[nonManys.size()]);
  }

  public BeanPropertyAssocMany<?>[] getMany() {
    return (BeanPropertyAssocMany[]) manys.toArray(new BeanPropertyAssocMany[manys.size()]);
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

  /**
   * Mode used to determine which BeanPropertyAssoc to include.
   */
  private enum Mode {
    Save, Delete, Validate;
  }

  private BeanPropertyAssocOne<?>[] getOne(boolean imported, Mode mode) {
    ArrayList<BeanPropertyAssocOne<?>> list = new ArrayList<BeanPropertyAssocOne<?>>();
    for (int i = 0; i < ones.size(); i++) {
      BeanPropertyAssocOne<?> prop = (BeanPropertyAssocOne<?>) ones.get(i);
      if (imported != prop.isOneToOneExported()) {
        switch (mode) {
        case Save:
          if (prop.getCascadeInfo().isSave()) {
            list.add(prop);
          }
          break;
        case Delete:
          if (prop.getCascadeInfo().isDelete()) {
            list.add(prop);
          }
          break;
        case Validate:
          if (prop.getCascadeInfo().isValidate()) {
            list.add(prop);
          }
          break;
        default:
          break;
        }
      }
    }

    return (BeanPropertyAssocOne[]) list.toArray(new BeanPropertyAssocOne[list.size()]);
  }

  private BeanPropertyAssocMany<?>[] getMany2Many() {
    ArrayList<BeanPropertyAssocMany<?>> list = new ArrayList<BeanPropertyAssocMany<?>>();
    for (int i = 0; i < manys.size(); i++) {
      BeanPropertyAssocMany<?> prop = (BeanPropertyAssocMany<?>) manys.get(i);
      if (prop.isManyToMany()) {
        list.add(prop);
      }
    }

    return (BeanPropertyAssocMany[]) list.toArray(new BeanPropertyAssocMany[list.size()]);
  }

  private BeanPropertyAssocMany<?>[] getMany(Mode mode) {
    ArrayList<BeanPropertyAssocMany<?>> list = new ArrayList<BeanPropertyAssocMany<?>>();
    for (int i = 0; i < manys.size(); i++) {
      BeanPropertyAssocMany<?> prop = (BeanPropertyAssocMany<?>) manys.get(i);

      switch (mode) {
      case Save:
        if (prop.getCascadeInfo().isSave() || prop.isManyToMany()
            || ModifyListenMode.REMOVALS.equals(prop.getModifyListenMode())) {
          // Note ManyToMany always included as we always 'save'
          // the relationship via insert/delete of intersection table
          // REMOVALS means including PrivateOwned relationships
          list.add(prop);
        }
        break;
      case Delete:
        if (prop.getCascadeInfo().isDelete() || ModifyListenMode.REMOVALS.equals(prop.getModifyListenMode())) {
          // REMOVALS means including PrivateOwned relationships
          list.add(prop);
        }
        break;
      case Validate:
        if (prop.getCascadeInfo().isValidate()) {
          list.add(prop);
        }
        break;
      default:
        break;
      }

    }

    return (BeanPropertyAssocMany[]) list.toArray(new BeanPropertyAssocMany[list.size()]);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private BeanProperty createBeanProperty(BeanDescriptorMap owner, DeployBeanProperty deployProp) {

    if (deployProp instanceof DeployBeanPropertyAssocOne) {
      return new BeanPropertyAssocOne(owner, desc, (DeployBeanPropertyAssocOne) deployProp);
    }
    
    if (deployProp instanceof DeployBeanPropertySimpleCollection<?>) {
      return new BeanPropertySimpleCollection(owner, desc, (DeployBeanPropertySimpleCollection) deployProp);
    }
    
    if (deployProp instanceof DeployBeanPropertyAssocMany) {
      return new BeanPropertyAssocMany(owner, desc, (DeployBeanPropertyAssocMany) deployProp);
    }
    
    if (deployProp instanceof DeployBeanPropertyCompound) {
      return new BeanPropertyCompound(owner, desc, (DeployBeanPropertyCompound) deployProp);
    }

    return new BeanProperty(owner, desc, deployProp);
  }
}
