package io.ebeaninternal.server.deploy.parse;

import io.ebeaninternal.server.deploy.InheritInfo;

import java.lang.reflect.Modifier;
import javax.persistence.DiscriminatorType;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the Inheritance tree.
 * Holds information regarding Super Subclass support.
 */
public class DeployInheritInfo {

  /**
   * the default discriminator column according to the JPA 1.0 spec.
   */
  private static final String DEFAULT_COLUMN_NAME = "dtype";

  private String discriminatorStringValue;
  private Object discriminatorObjectValue;

  private int columnType;
  private String columnName;
  private int columnLength;
  private String columnDefn;

  private final Class<?> type;

  private Class<?> parent;

  private final ArrayList<DeployInheritInfo> children = new ArrayList<>();

  /**
   * Create for a given type.
   */
  DeployInheritInfo(Class<?> type) {
    this.type = type;
  }

  /**
   * return the type.
   */
  public Class<?> getType() {
    return type;
  }

  /**
   * Return the type of the root object.
   */
  public Class<?> getParent() {
    return parent;
  }

  /**
   * Set the type of the root object.
   */
  public void setParent(Class<?> parent) {
    this.parent = parent;
  }

  /**
   * Return true if this is abstract node.
   */
  public boolean isAbstract() {
    return Modifier.isAbstract(type.getModifiers());
  }

  /**
   * Return true if this is the root node.
   */
  public boolean isRoot() {
    return parent == null;
  }

  /**
   * Return the child nodes.
   */
  public List<DeployInheritInfo> children() {
    return children;
  }

  /**
   * Add a child node.
   */
  public void addChild(DeployInheritInfo childInfo) {
    children.add(childInfo);
  }

  /**
   * Return the column name of the discriminator.
   */
  public String getColumnName(InheritInfo parent) {
    if (columnName == null) {
      if (parent == null) {
        columnName = DEFAULT_COLUMN_NAME;
      } else {
        columnName = parent.getDiscriminatorColumn();
      }
    }
    return columnName;
  }

  /**
   * Set the column name of the discriminator.
   */
  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public int getColumnLength(InheritInfo parent) {
    if (columnLength == 0) {
      if (parent == null) {
        columnLength = 31;
      } else {
        columnLength = parent.getColumnLength();
      }
    }
    return columnLength;
  }

  /**
   * Return the sql type of the discriminator value.
   */
  public int getDiscriminatorType(InheritInfo parent) {
    if (columnType == 0) {
      if (parent == null) {
        columnType = Types.VARCHAR;
      } else {
        columnType = parent.getDiscriminatorType();
      }
    }
    return columnType;
  }

  /**
   * Set the sql type of the discriminator.
   */
  public void setColumnType(DiscriminatorType type) {
    if (type == DiscriminatorType.INTEGER) {
      this.columnType = Types.INTEGER;
    } else {
      this.columnType = Types.VARCHAR;
    }
  }

  /**
   * Set explicit column definition (ddl).
   */
  void setColumnDefn(String columnDefn) {
    this.columnDefn = columnDefn;
  }

  /**
   * Return the explicit column definition.
   */
  public String getColumnDefn() {
    return columnDefn;
  }

  /**
   * Set the length of the discriminator column.
   */
  void setColumnLength(int columnLength) {
    this.columnLength = columnLength;
  }

  /**
   * Return the discriminator value for this node.
   */
  public Object getDiscriminatorObjectValue() {
    return discriminatorObjectValue;
  }

  public String getDiscriminatorStringValue() {
    return discriminatorStringValue;
  }

  /**
   * Set the discriminator value for this node.
   */
  void setDiscriminatorValue(String value) {
    if (value != null) {
      value = value.trim();
      if (!value.isEmpty()) {
        discriminatorStringValue = value;
        // convert the value if desired
        if (columnType == Types.INTEGER) {
          this.discriminatorObjectValue = Integer.valueOf(value);
        } else {
          this.discriminatorObjectValue = value;
        }
      }
    }
  }

  public String getWhere() {

    List<Object> discList = new ArrayList<>();

    appendDiscriminator(discList);

    return buildWhereLiteral(discList);
  }

  private void appendDiscriminator(List<Object> list) {
    if (!isAbstract()) {
      list.add(discriminatorObjectValue);
    }
    for (DeployInheritInfo child : children) {
      child.appendDiscriminator(list);
    }
  }

  private String buildWhereLiteral(List<Object> discList) {
    int size = discList.size();
    if (size == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append(columnName);
    if (size == 1) {
      sb.append(" = ");
    } else {
      sb.append(" in (");
    }
    for (int i = 0; i < discList.size(); i++) {
      appendSqlLiteralValue(i, discList.get(i), sb);
    }
    if (size > 1) {
      sb.append(")");
    }
    return sb.toString();
  }

  private void appendSqlLiteralValue(int count, Object value, StringBuilder sb) {
    if (count > 0) {
      sb.append(",");
    }
    if (value instanceof String) {
      sb.append("'").append(value).append("'");
    } else {
      sb.append(value);
    }
  }

  @Override
  public String toString() {
    return "InheritInfo[" + type.getName() + "]" + " root[" + parent.getName() + "]" + " disValue[" + discriminatorStringValue + "]";
  }

}
