
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
 *       &lt;attribute name="columnName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="tableName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="notnull" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="historyExclude" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="unique" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="uniqueOneToOne" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="oldDefaultValue" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="newDefaultValue" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="oldCheckConstraint" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="newCheckConstraint" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="oldReferences" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="newReferences" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "alterColumn")
public class AlterColumn {

    @XmlAttribute(name = "columnName", required = true)
    protected String columnName;
    @XmlAttribute(name = "tableName", required = true)
    protected String tableName;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "notnull")
    protected Boolean notnull;
    @XmlAttribute(name = "historyExclude")
    protected Boolean historyExclude;
    @XmlAttribute(name = "unique")
    protected Boolean unique;
    @XmlAttribute(name = "uniqueOneToOne")
    protected Boolean uniqueOneToOne;
    @XmlAttribute(name = "oldDefaultValue")
    protected String oldDefaultValue;
    @XmlAttribute(name = "newDefaultValue")
    protected String newDefaultValue;
    @XmlAttribute(name = "oldCheckConstraint")
    protected String oldCheckConstraint;
    @XmlAttribute(name = "newCheckConstraint")
    protected String newCheckConstraint;
    @XmlAttribute(name = "oldReferences")
    protected String oldReferences;
    @XmlAttribute(name = "newReferences")
    protected String newReferences;

    /**
     * Gets the value of the columnName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Sets the value of the columnName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColumnName(String value) {
        this.columnName = value;
    }

    /**
     * Gets the value of the tableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the value of the tableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTableName(String value) {
        this.tableName = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the notnull property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNotnull() {
        return notnull;
    }

    /**
     * Sets the value of the notnull property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNotnull(Boolean value) {
        this.notnull = value;
    }

    /**
     * Gets the value of the historyExclude property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHistoryExclude() {
        return historyExclude;
    }

    /**
     * Sets the value of the historyExclude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHistoryExclude(Boolean value) {
        this.historyExclude = value;
    }

    /**
     * Gets the value of the unique property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUnique() {
        return unique;
    }

    /**
     * Sets the value of the unique property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUnique(Boolean value) {
        this.unique = value;
    }

    /**
     * Gets the value of the uniqueOneToOne property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUniqueOneToOne() {
        return uniqueOneToOne;
    }

    /**
     * Sets the value of the uniqueOneToOne property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUniqueOneToOne(Boolean value) {
        this.uniqueOneToOne = value;
    }

    /**
     * Gets the value of the oldDefaultValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldDefaultValue() {
        return oldDefaultValue;
    }

    /**
     * Sets the value of the oldDefaultValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldDefaultValue(String value) {
        this.oldDefaultValue = value;
    }

    /**
     * Gets the value of the newDefaultValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewDefaultValue() {
        return newDefaultValue;
    }

    /**
     * Sets the value of the newDefaultValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewDefaultValue(String value) {
        this.newDefaultValue = value;
    }

    /**
     * Gets the value of the oldCheckConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldCheckConstraint() {
        return oldCheckConstraint;
    }

    /**
     * Sets the value of the oldCheckConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldCheckConstraint(String value) {
        this.oldCheckConstraint = value;
    }

    /**
     * Gets the value of the newCheckConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewCheckConstraint() {
        return newCheckConstraint;
    }

    /**
     * Sets the value of the newCheckConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewCheckConstraint(String value) {
        this.newCheckConstraint = value;
    }

    /**
     * Gets the value of the oldReferences property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldReferences() {
        return oldReferences;
    }

    /**
     * Sets the value of the oldReferences property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldReferences(String value) {
        this.oldReferences = value;
    }

    /**
     * Gets the value of the newReferences property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewReferences() {
        return newReferences;
    }

    /**
     * Sets the value of the newReferences property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewReferences(String value) {
        this.newReferences = value;
    }

}
