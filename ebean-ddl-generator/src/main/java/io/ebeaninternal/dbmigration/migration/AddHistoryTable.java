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
 *       &lt;attribute name="baseTable" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "addHistoryTable")
public class AddHistoryTable {

  @XmlAttribute(name = "baseTable", required = true)
  protected String baseTable;

  /**
   * Gets the value of the baseTable property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getBaseTable() {
    return baseTable;
  }

  /**
   * Sets the value of the baseTable property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setBaseTable(String value) {
    this.baseTable = value;
  }

}
