package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.InternString;

/**
 * The Exported foreign key and property.
 * <p>
 * Used to for Assoc Manys to create references etc.
 * </p>
 */
public class ExportedProperty {

  private final String foreignDbColumn;

  private final BeanProperty property;

  private final boolean embedded;

  public ExportedProperty(boolean embedded, String foreignDbColumn, BeanProperty property) {
    this.embedded = embedded;
    this.foreignDbColumn = InternString.intern(foreignDbColumn);
    this.property = property;
  }

  /**
   * Return true if this is part of an embedded concatinated key.
   */
  public boolean isEmbedded() {
    return embedded;
  }

  /**
   * Return the property value from the bean.
   */
  public Object getValue(EntityBean bean) {
    return property.getValue(bean);
  }

  /**
   * Return the foreign database column matching this property.
   * <p>
   * We use this foreign database column in the query predicates
   * in preference to a parentProperty.idProperty = value.
   * Just using the foreign database column avoids triggering
   * a join to the 'parent' table.
   * </p>
   */
  public String getForeignDbColumn() {
    return foreignDbColumn;
  }

  /**
   * Append a logical where for the foreign db column to logical property name,
   */
  public void appendWhere(StringBuilder sb, String alias, String path) {
    sb.append(alias).append(foreignDbColumn).append(" = ");
    if (path != null) {
      sb.append(path).append(".");
    }
    sb.append(property.getName());
  }
}
