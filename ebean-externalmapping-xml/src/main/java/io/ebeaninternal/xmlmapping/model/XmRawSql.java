package io.ebeaninternal.xmlmapping.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/ebean}alias-mapping" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/ebean}column-mapping" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/ebean}query"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
  "aliasMapping",
  "columnMapping",
  "query"
})
@XmlRootElement(name = "raw-sql")
public class XmRawSql {

  @XmlElement(name = "alias-mapping")
  protected List<XmAliasMapping> aliasMapping;
  @XmlElement(name = "column-mapping")
  protected List<XmColumnMapping> columnMapping;
  @XmlElement(required = true)
  protected XmQuery query;
  @XmlAttribute(name = "name", required = true)
  protected String name;

  /**
   * Gets the value of the aliasMapping property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the aliasMapping property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getAliasMapping().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link XmAliasMapping }
   */
  public List<XmAliasMapping> getAliasMapping() {
    if (aliasMapping == null) {
      aliasMapping = new ArrayList<>();
    }
    return this.aliasMapping;
  }

  /**
   * Gets the value of the columnMapping property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the columnMapping property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getColumnMapping().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link XmColumnMapping }
   */
  public List<XmColumnMapping> getColumnMapping() {
    if (columnMapping == null) {
      columnMapping = new ArrayList<>();
    }
    return this.columnMapping;
  }

  /**
   * Gets the value of the query property.
   *
   * @return possible object is
   * {@link XmQuery }
   */
  public XmQuery getQuery() {
    return query;
  }

  /**
   * Sets the value of the query property.
   *
   * @param value allowed object is
   *              {@link XmQuery }
   */
  public void setQuery(XmQuery value) {
    this.query = value;
  }

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

}
