package io.ebeaninternal.xmlmapping.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/ebean}entity" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
  "entity",
  "dto"
})
@XmlRootElement(name = "ebean")
public class XmEbean {

  @XmlElement(required = false)
  protected List<XmEntity> entity;

  @XmlElement(required = false)
  protected List<XmDto> dto;

  /**
   * Gets the value of the entity property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the entity property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getEntity().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link XmEntity }
   */
  public List<XmEntity> getEntity() {
    if (entity == null) {
      entity = new ArrayList<>();
    }
    return this.entity;
  }

  /**
   * Returns the List of XmDto beans.
   */
  public List<XmDto> getDto() {
    if (dto == null) {
      dto = new ArrayList<>();
    }
    return this.dto;
  }

}
