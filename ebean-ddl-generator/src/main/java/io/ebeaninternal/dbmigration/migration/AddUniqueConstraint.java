package io.ebeaninternal.dbmigration.migration;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="constraintName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="tableName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="columnNames" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="oneToOne" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="nullableColumns" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="platforms" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
  @XmlAttribute(name = "platforms")
  protected String platforms;

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

  /**
   * Gets the value of the platforms property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getPlatforms() {
    return platforms;
  }

  /**
   * Sets the value of the platforms property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setPlatforms(String value) {
    this.platforms = value;
  }

}
