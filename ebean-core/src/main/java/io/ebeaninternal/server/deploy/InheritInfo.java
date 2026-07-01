package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.InternString;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.parse.DeployInheritInfo;
import io.ebeaninternal.server.query.SqlTreeProperties;

import jakarta.persistence.PersistenceException;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Represents a node in the Inheritance tree. Holds information regarding Super Subclass support.
 */
public final class InheritInfo {

  private final String discriminatorStringValue;
  private final Object discriminatorValue;
  private final String discriminatorColumn;
  private final int discriminatorType;
  private final int discriminatorLength;
  private final String columnDefn;
  private final String where;
  private final Class<?> type;
  private final List<InheritInfo> children = new ArrayList<>();
  /**
   * Map of discriminator values to InheritInfo.
   */
  private final HashMap<String, InheritInfo> discMap;
  /**
   * Map of class types to InheritInfo (taking into account subclass proxy classes).
   */
  private final HashMap<String, InheritInfo> typeMap;
  private final InheritInfo parent;
  private final InheritInfo root;
  private BeanDescriptor<?> descriptor;

  public InheritInfo(InheritInfo r, InheritInfo parent, DeployInheritInfo deploy) {
    this.parent = parent;
    this.type = deploy.getType();
    this.discriminatorColumn = InternString.intern(deploy.getColumnName(parent));
    this.discriminatorValue = deploy.getDiscriminatorObjectValue();
    this.discriminatorStringValue = deploy.getDiscriminatorStringValue();
    this.discriminatorType = deploy.getDiscriminatorType(parent);
    this.discriminatorLength = deploy.getColumnLength(parent);
    this.columnDefn = deploy.getColumnDefn();
    this.where = InternString.intern(deploy.getWhere());
    if (r == null) {
      // this is a root node
      root = this;
      discMap = new HashMap<>();
      typeMap = new HashMap<>();
      registerWithRoot(this);
    } else {
      this.root = r;
      // register with the root node...
      discMap = null;
      typeMap = null;
      root.registerWithRoot(this);
    }
  }

  /**
   * Visit all the children in the inheritance tree.
   */
  public void visitChildren(InheritInfoVisitor visitor) {
    for (InheritInfo child : children) {
      visitor.visit(child);
      child.visitChildren(visitor);
    }
  }

  /**
   * Append check constraint values for the entire inheritance hierarchy.
   */
  public void appendCheckConstraintValues(final String propertyName, final Set<String> checkConstraintValues) {
    visitChildren(inheritInfo -> {
      BeanProperty prop = inheritInfo.desc().beanProperty(propertyName);
      if (prop != null) {
        Set<String> values = prop.dbCheckConstraintValues();
        if (values != null) {
          checkConstraintValues.addAll(values);
        }
      }
    });
  }

  /**
   * return true if anything in the inheritance hierarchy has a relationship with a save cascade on
   * it.
   */
  boolean isSaveRecurseSkippable() {
    return root.isNodeSaveRecurseSkippable();
  }

  private boolean isNodeSaveRecurseSkippable() {
    if (!descriptor.isSaveRecurseSkippable()) {
      return false;
    }
    for (InheritInfo child : children) {
      if (!child.isNodeSaveRecurseSkippable()) {
        return false;
      }
    }
    return true;
  }

  /**
   * return true if anything in the inheritance hierarchy has a relationship with a delete cascade
   * on it.
   */
  boolean isDeleteRecurseSkippable() {
    return root.isNodeDeleteRecurseSkippable();
  }

  private boolean isNodeDeleteRecurseSkippable() {
    if (!descriptor.isDeleteRecurseSkippable()) {
      return false;
    }
    for (InheritInfo child : children) {
      if (!child.isNodeDeleteRecurseSkippable()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Set the descriptor for this node.
   */
  public void setDescriptor(BeanDescriptor<?> descriptor) {
    this.descriptor = descriptor;
  }

  /**
   * Return the associated BeanDescriptor for this node.
   */
  public BeanDescriptor<?> desc() {
    return descriptor;
  }

  /**
   * Return the local properties for this node in the hierarchy.
   */
  public BeanProperty[] localProperties() {
    return descriptor.propertiesLocal();
  }

  /**
   * Return the children.
   */
  public List<InheritInfo> getChildren() {
    return children;
  }

  /**
   * Return true if this node has children.
   * <p>
   * When an inheritance node has no children then we don't need
   * the discriminator column as the type is effectively known.
   */
  public boolean hasChildren() {
    return !children.isEmpty();
  }

  /**
   * Get the bean property additionally looking in the sub types.
   */
  BeanProperty findSubTypeProperty(String propertyName) {
    BeanProperty prop;
    for (InheritInfo childInfo : children) {
      // recursively search this child bean descriptor
      prop = childInfo.desc().findProperty(propertyName);
      if (prop != null) {
        return prop;
      }
    }
    return null;
  }

  /**
   * Add the local properties for each sub class below this one.
   */
  public void addChildrenProperties(SqlTreeProperties selectProps) {
    for (InheritInfo childInfo : children) {
      selectProps.add(childInfo.descriptor.propertiesLocal());
      childInfo.addChildrenProperties(selectProps);
    }
  }

  /**
   * Return the associated InheritInfo for this DB row read.
   */
  public InheritInfo readType(DbReadContext ctx) throws SQLException {
    return readType(ctx.dataReader().getString());
  }

  /**
   * Return the associated InheritInfo for this discriminator value.
   */
  InheritInfo readType(String discValue) {
    if (discValue == null) {
      return null;
    }
    InheritInfo typeInfo = root.getType(discValue);
    if (typeInfo == null) {
      throw new PersistenceException("Inheritance type for discriminator value [" + discValue + "] was not found?");
    }
    return typeInfo;
  }

  /**
   * Return the associated InheritInfo for this bean type.
   */
  public InheritInfo readType(Class<?> beanType) {
    InheritInfo typeInfo = root.getTypeByClass(beanType);
    if (typeInfo == null) {
      throw new PersistenceException("Inheritance type for bean type [" + beanType.getName() + "] was not found?");
    }
    return typeInfo;
  }

  /**
   * Create an EntityBean for this type.
   */
  public EntityBean createEntityBean(boolean unmodifiable) {
    return descriptor.createEntityBean2(unmodifiable);
  }

  /**
   * Return the IdBinder for this type.
   */
  public IdBinder getIdBinder() {
    return descriptor.idBinder();
  }

  /**
   * return the type.
   */
  public Class<?> getType() {
    return type;
  }

  /**
   * Return the root node of the tree.
   * <p>
   * The root has a map of discriminator values to types.
   * </p>
   */
  public InheritInfo getRoot() {
    return root;
  }

  /**
   * Return the parent node.
   */
  public InheritInfo getParent() {
    return parent;
  }

  /**
   * Return true if this is the root node.
   */
  public boolean isRoot() {
    return parent == null;
  }

  /**
   * Return true if this is considered a concrete type in the inheritance hierarchy.
   */
  public boolean isConcrete() {
    return !Modifier.isAbstract(type.getModifiers());
  }

  /**
   * For a discriminator get the inheritance information for this tree.
   */
  public InheritInfo getType(String discValue) {
    return discMap.get(discValue);
  }

  /**
   * Return the InheritInfo for the given bean type.
   */
  private InheritInfo getTypeByClass(Class<?> beanType) {
    return typeMap.get(beanType.getName());
  }

  private void registerWithRoot(InheritInfo info) {
    if (info.getDiscriminatorStringValue() != null) {
      String stringDiscValue = info.getDiscriminatorStringValue();
      discMap.put(stringDiscValue, info);
    }
    typeMap.put(info.getType().getName(), info);
  }

  /**
   * Add a child node.
   */
  public void addChild(InheritInfo childInfo) {
    children.add(childInfo);
  }

  /**
   * Return the derived where for the discriminator.
   */
  public String getWhere() {
    return where;
  }

  /**
   * Return the column name of the discriminator.
   */
  public String getDiscriminatorColumn() {
    return discriminatorColumn;
  }

  /**
   * Return the sql type of the discriminator value.
   */
  public int getDiscriminatorType() {
    return discriminatorType;
  }

  /**
   * Return the length of the discriminator column.
   */
  public int getColumnLength() {
    return discriminatorLength;
  }

  /**
   * Return the explicit column definition.
   */
  public String getColumnDefn() {
    return columnDefn;
  }

  /**
   * Return the discriminator value for this node.
   */
  String getDiscriminatorStringValue() {
    return discriminatorStringValue;
  }

  public Object getDiscriminatorValue() {
    return discriminatorValue;
  }

  @Override
  public String toString() {
    return "InheritInfo " + type.getName() + " disc:" + discriminatorStringValue;
  }

}
