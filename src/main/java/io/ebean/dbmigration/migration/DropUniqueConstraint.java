package io.ebean.dbmigration.migration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * TODO
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "createUniqueConstraint")
public class DropUniqueConstraint {

  @XmlAttribute(name = "constraintName", required = true)
  protected String constraintName;
  @XmlAttribute(name = "tableName", required = true)
  protected String tableName;

  /**
   * Gets the value of the constraintName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getConstraintName() {
    return constraintName;
  }

  /**
   * Sets the value of the constraintName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setConstraintName(String value) {
    this.constraintName = value;
  }

  /**
   * Gets the value of the tableName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Sets the value of the tableName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
  
}
