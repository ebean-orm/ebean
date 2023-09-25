package io.ebeaninternal.dbmigration.migration;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="sequenceCol" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="sequenceName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "dropTable")
public class DropTable {

  @XmlAttribute(name = "name", required = true)
  protected String name;
  @XmlAttribute(name = "sequenceCol")
  protected String sequenceCol;
  @XmlAttribute(name = "sequenceName")
  protected String sequenceName;

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
   * Gets the value of the sequenceCol property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getSequenceCol() {
    return sequenceCol;
  }

  /**
   * Sets the value of the sequenceCol property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setSequenceCol(String value) {
    this.sequenceCol = value;
  }

  /**
   * Gets the value of the sequenceName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getSequenceName() {
    return sequenceName;
  }

  /**
   * Sets the value of the sequenceName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setSequenceName(String value) {
    this.sequenceName = value;
  }

}
