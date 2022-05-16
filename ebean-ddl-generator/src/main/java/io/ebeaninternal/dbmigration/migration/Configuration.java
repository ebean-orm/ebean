package io.ebeaninternal.dbmigration.migration;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/dbmigration}defaultTablespace"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
   * @return possible object is
   * {@link DefaultTablespace }
   */
  public DefaultTablespace getDefaultTablespace() {
    return defaultTablespace;
  }

  /**
   * Sets the value of the defaultTablespace property.
   *
   * @param value allowed object is
   *              {@link DefaultTablespace }
   */
  public void setDefaultTablespace(DefaultTablespace value) {
    this.defaultTablespace = value;
  }

}
