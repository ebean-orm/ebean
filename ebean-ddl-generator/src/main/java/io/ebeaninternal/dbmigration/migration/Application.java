package io.ebeaninternal.dbmigration.migration;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="resourcePath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "application")
public class Application {

  @XmlAttribute(name = "name", required = true)
  protected String name;
  @XmlAttribute(name = "resourcePath", required = true)
  protected String resourcePath;

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

  /**
   * Gets the value of the resourcePath property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getResourcePath() {
    return resourcePath;
  }

  /**
   * Sets the value of the resourcePath property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setResourcePath(String value) {
    this.resourcePath = value;
  }

}
