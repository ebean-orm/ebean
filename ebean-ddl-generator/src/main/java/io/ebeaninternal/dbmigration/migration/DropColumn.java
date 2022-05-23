package io.ebeaninternal.dbmigration.migration;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="columnName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="tableName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="withHistory" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "dropColumn")
public class DropColumn {

  @XmlAttribute(name = "columnName", required = true)
  protected String columnName;
  @XmlAttribute(name = "tableName", required = true)
  protected String tableName;
  @XmlAttribute(name = "withHistory")
  protected Boolean withHistory;

  /**
   * Gets the value of the columnName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getColumnName() {
    return columnName;
  }

  /**
   * Sets the value of the columnName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setColumnName(String value) {
    this.columnName = value;
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
   * Gets the value of the withHistory property.
   *
   * @return possible object is
   * {@link Boolean }
   */
  public Boolean isWithHistory() {
    return withHistory;
  }

  /**
   * Sets the value of the withHistory property.
   *
   * @param value allowed object is
   *              {@link Boolean }
   */
  public void setWithHistory(Boolean value) {
    this.withHistory = value;
  }

}
