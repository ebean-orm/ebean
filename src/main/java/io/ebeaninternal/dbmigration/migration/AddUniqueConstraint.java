package io.ebeaninternal.dbmigration.migration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="constraintName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="tableName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="columnNames" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="oneToOne" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="nullableColumns" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "addUniqueConstraint")
public class AddUniqueConstraint {

  @XmlAttribute(name = "constraintName", required = true)
  protected String constraintName;
  @XmlAttribute(name = "tableName", required = true)
  protected String tableName;
  @XmlAttribute(name = "columnNames", required = true)
  protected String columnNames;
  @XmlAttribute(name = "oneToOne")
  protected Boolean oneToOne;
  @XmlAttribute(name = "nullableColumns")
  protected String nullableColumns;

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
  public void setTableName(String value) {
    this.tableName = value;
  }

  /**
   * Gets the value of the columnNames property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getColumnNames() {
    return columnNames;
  }

  /**
   * Sets the value of the columnNames property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setColumnNames(String value) {
    this.columnNames = value;
  }

  /**
   * Gets the value of the oneToOne property.
   *
   * @return possible object is
   * {@link Boolean }
   */
  public Boolean isOneToOne() {
    return oneToOne;
  }

  /**
   * Sets the value of the oneToOne property.
   *
   * @param value allowed object is
   *              {@link Boolean }
   */
  public void setOneToOne(Boolean value) {
    this.oneToOne = value;
  }

  /**
   * Gets the value of the nullableColumns property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getNullableColumns() {
    return nullableColumns;
  }

  /**
   * Sets the value of the nullableColumns property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setNullableColumns(String value) {
    this.nullableColumns = value;
  }

}
