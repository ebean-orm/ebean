
package com.avaje.ebeaninternal.server.autotune.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/autotune}origin" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/autotune}profileDiff" minOccurs="0"/>
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/autotune}profileNew" minOccurs="0"/>
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/autotune}profileEmpty" minOccurs="0"/>
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
    "origin",
    "profileDiff",
    "profileNew",
    "profileEmpty"
})
@XmlRootElement(name = "autotune")
public class Autotune {

    protected List<Origin> origin;
    protected ProfileDiff profileDiff;
    protected ProfileNew profileNew;
    protected ProfileEmpty profileEmpty;

    /**
     * Gets the value of the origin property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the origin property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrigin().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Origin }
     * 
     * 
     */
    public List<Origin> getOrigin() {
        if (origin == null) {
            origin = new ArrayList<Origin>();
        }
        return this.origin;
    }

    /**
     * Gets the value of the profileDiff property.
     * 
     * @return
     *     possible object is
     *     {@link ProfileDiff }
     *     
     */
    public ProfileDiff getProfileDiff() {
        return profileDiff;
    }

    /**
     * Sets the value of the profileDiff property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProfileDiff }
     *     
     */
    public void setProfileDiff(ProfileDiff value) {
        this.profileDiff = value;
    }

    /**
     * Gets the value of the profileNew property.
     * 
     * @return
     *     possible object is
     *     {@link ProfileNew }
     *     
     */
    public ProfileNew getProfileNew() {
        return profileNew;
    }

    /**
     * Sets the value of the profileNew property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProfileNew }
     *     
     */
    public void setProfileNew(ProfileNew value) {
        this.profileNew = value;
    }

    /**
     * Gets the value of the profileEmpty property.
     * 
     * @return
     *     possible object is
     *     {@link ProfileEmpty }
     *     
     */
    public ProfileEmpty getProfileEmpty() {
        return profileEmpty;
    }

    /**
     * Sets the value of the profileEmpty property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProfileEmpty }
     *     
     */
    public void setProfileEmpty(ProfileEmpty value) {
        this.profileEmpty = value;
    }

}
