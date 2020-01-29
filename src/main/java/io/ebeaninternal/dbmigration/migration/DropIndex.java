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
 *       &lt;attribute name="concurrent" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="platforms" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "dropIndex")
public class DropIndex {

  @XmlAttribute(name = "indexName", required = true)
  protected String indexName;
  @XmlAttribute(name = "tableName", required = true)
  protected String tableName;
  @XmlAttribute(name = "concurrent")
  protected Boolean concurrent;
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
