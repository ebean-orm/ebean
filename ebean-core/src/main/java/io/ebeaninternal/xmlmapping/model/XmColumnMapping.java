package io.ebeaninternal.xmlmapping.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="column" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="property" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "column-mapping")
public class XmColumnMapping {

  @XmlAttribute(name = "column", required = true)
  protected String column;
  @XmlAttribute(name = "property", required = true)
  protected String property;

  /**
   * Gets the value of the column property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getColumn() {
    return column;
  }

  /**
   * Sets the value of the column property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setColumn(String value) {
    this.column = value;
  }

  /**
   * Gets the value of the property property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getProperty() {
    return property;
  }

  /**
   * Sets the value of the property property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setProperty(String value) {
    this.property = value;
  }

}
