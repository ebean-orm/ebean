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
 *       &lt;attribute name="oldName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="newName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "renameTable")
public class RenameTable {

  @XmlAttribute(name = "oldName", required = true)
  protected String oldName;
  @XmlAttribute(name = "newName", required = true)
  protected String newName;

  /**
   * Gets the value of the oldName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getOldName() {
    return oldName;
  }

  /**
   * Sets the value of the oldName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setOldName(String value) {
    this.oldName = value;
  }

  /**
   * Gets the value of the newName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getNewName() {
    return newName;
  }

  /**
   * Sets the value of the newName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setNewName(String value) {
    this.newName = value;
  }

}
