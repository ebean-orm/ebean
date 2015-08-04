
package com.avaje.ebean.dbmigration.migration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/dbmigration}defaultTablespace"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "defaultTablespace"
})
@XmlRootElement(name = "configuration")
public class Configuration {

    @XmlElement(required = true)
    protected DefaultTablespace defaultTablespace;

    /**
     * Gets the value of the defaultTablespace property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultTablespace }
     *     
     */
    public DefaultTablespace getDefaultTablespace() {
        return defaultTablespace;
    }

    /**
     * Sets the value of the defaultTablespace property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultTablespace }
     *     
     */
    public void setDefaultTablespace(DefaultTablespace value) {
        this.defaultTablespace = value;
    }

}
