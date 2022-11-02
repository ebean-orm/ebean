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
 *       &lt;attribute name="tables" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="indexes" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="history" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "defaultTablespace")
public class DefaultTablespace {

  @XmlAttribute(name = "tables")
  protected String tables;
  @XmlAttribute(name = "indexes")
  protected String indexes;
  @XmlAttribute(name = "history")
  protected String history;

  /**
   * Gets the value of the tables property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getTables() {
    return tables;
  }

  /**
   * Sets the value of the tables property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setTables(String value) {
    this.tables = value;
  }

  /**
   * Gets the value of the indexes property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getIndexes() {
    return indexes;
  }

  /**
   * Sets the value of the indexes property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setIndexes(String value) {
    this.indexes = value;
  }

  /**
   * Gets the value of the history property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getHistory() {
    return history;
  }

  /**
   * Sets the value of the history property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setHistory(String value) {
    this.history = value;
  }

}
