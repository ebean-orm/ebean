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
 *       &lt;attribute name="indexName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="tableName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="columns" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="unique" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="concurrent" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="definition" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="platforms" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "createIndex")
public class CreateIndex {

  @XmlAttribute(name = "indexName", required = true)
  protected String indexName;
  @XmlAttribute(name = "tableName", required = true)
  protected String tableName;
  @XmlAttribute(name = "columns", required = true)
  protected String columns;
  @XmlAttribute(name = "unique")
  protected Boolean unique;
  @XmlAttribute(name = "concurrent")
  protected Boolean concurrent;
  @XmlAttribute(name = "definition")
  protected String definition;
  @XmlAttribute(name = "platforms")
  protected String platforms;

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
   * Gets the value of the columns property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getColumns() {
    return columns;
  }

  /**
   * Sets the value of the columns property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setColumns(String value) {
    this.columns = value;
  }

  /**
   * Gets the value of the unique property.
   *
   * @return possible object is
   * {@link Boolean }
   */
  public Boolean isUnique() {
    return unique;
  }

  /**
   * Sets the value of the unique property.
   *
   * @param value allowed object is
   *              {@link Boolean }
   */
  public void setUnique(Boolean value) {
    this.unique = value;
  }

  /**
   * Gets the value of the concurrent property.
   *
   * @return possible object is
   * {@link Boolean }
   */
  public Boolean isConcurrent() {
    return concurrent;
  }

  /**
   * Sets the value of the concurrent property.
   *
   * @param value allowed object is
   *              {@link Boolean }
   */
  public void setConcurrent(Boolean value) {
    this.concurrent = value;
  }

  /**
   * Gets the value of the definition property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getDefinition() {
    return definition;
  }

  /**
   * Sets the value of the definition property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setDefinition(String value) {
    this.definition = value;
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
