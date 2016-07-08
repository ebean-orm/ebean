
package com.avaje.ebeaninternal.xmlmapping.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
 *       &lt;sequence>
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/ebean}raw-sql" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="class" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "rawSql"
})
@XmlRootElement(name = "entity")
public class XmEntity {

    @XmlElement(name = "raw-sql", required = true)
    protected List<XmRawSql> rawSql;
    @XmlAttribute(name = "class", required = true)
    protected String clazz;

    /**
     * Gets the value of the rawSql property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rawSql property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRawSql().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XmRawSql }
     * 
     * 
     */
    public List<XmRawSql> getRawSql() {
        if (rawSql == null) {
            rawSql = new ArrayList<XmRawSql>();
        }
        return this.rawSql;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

}
