
package com.avaje.ebean.dbmigration.migration;

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
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="columnNames" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="refColumnNames" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="refTableName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="indexName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
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

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the columnNames property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColumnNames() {
        return columnNames;
    }

    /**
     * Sets the value of the columnNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColumnNames(String value) {
        this.columnNames = value;
    }

    /**
     * Gets the value of the refColumnNames property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefColumnNames() {
        return refColumnNames;
    }

    /**
     * Sets the value of the refColumnNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefColumnNames(String value) {
        this.refColumnNames = value;
    }

    /**
     * Gets the value of the refTableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefTableName() {
        return refTableName;
    }

    /**
     * Sets the value of the refTableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefTableName(String value) {
        this.refTableName = value;
    }

    /**
     * Gets the value of the indexName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * Sets the value of the indexName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIndexName(String value) {
        this.indexName = value;
    }

}
