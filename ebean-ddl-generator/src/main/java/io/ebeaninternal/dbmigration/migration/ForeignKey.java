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
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="columnNames" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="refColumnNames" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="refTableName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="indexName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="onDelete" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="onUpdate" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "foreignKey")
public class ForeignKey {

  @XmlAttribute(name = "name", required = true)
  protected String name;
  @XmlAttribute(name = "columnNames", required = true)
  protected String columnNames;
  @XmlAttribute(name = "refColumnNames", required = true)
  protected String refColumnNames;
  @XmlAttribute(name = "refTableName", required = true)
  protected String refTableName;
  @XmlAttribute(name = "indexName")
  protected String indexName;
  @XmlAttribute(name = "onDelete")
  protected String onDelete;
  @XmlAttribute(name = "onUpdate")
  protected String onUpdate;

  /**
   * Gets the value of the name property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setName(String value) {
    this.name = value;
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
   * Gets the value of the refColumnNames property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getRefColumnNames() {
    return refColumnNames;
  }

  /**
   * Sets the value of the refColumnNames property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setRefColumnNames(String value) {
    this.refColumnNames = value;
  }

  /**
   * Gets the value of the refTableName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getRefTableName() {
    return refTableName;
  }

  /**
   * Sets the value of the refTableName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setRefTableName(String value) {
    this.refTableName = value;
  }

  /**
   * Gets the value of the indexName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getIndexName() {
    return indexName;
  }

  /**
   * Sets the value of the indexName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setIndexName(String value) {
    this.indexName = value;
  }

  /**
   * Gets the value of the onDelete property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getOnDelete() {
    return onDelete;
  }

  /**
   * Sets the value of the onDelete property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setOnDelete(String value) {
    this.onDelete = value;
  }

  /**
   * Gets the value of the onUpdate property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getOnUpdate() {
    return onUpdate;
  }

  /**
   * Sets the value of the onUpdate property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setOnUpdate(String value) {
    this.onUpdate = value;
  }

}
