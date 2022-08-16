package io.ebeaninternal.dbmigration.migration;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


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
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/dbmigration}column" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="tableName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="withHistory" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
  "column"
})
@XmlRootElement(name = "addColumn")
public class AddColumn {

  @XmlElement(required = true)
  protected List<Column> column;
  @XmlAttribute(name = "tableName", required = true)
  protected String tableName;
  @XmlAttribute(name = "withHistory")
  protected Boolean withHistory;

  /**
   * Gets the value of the column property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the Jakarta XML Binding object.
   * This is why there is not a <CODE>set</CODE> method for the column property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getColumn().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link Column }
   */
  public List<Column> getColumn() {
    if (column == null) {
      column = new ArrayList<>();
    }
    return this.column;
  }

  /**
   * Gets the value of the tableName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Sets the value of the tableName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setTableName(String value) {
    this.tableName = value;
  }

  /**
   * Gets the value of the withHistory property.
   *
   * @return possible object is
   * {@link Boolean }
   */
  public Boolean isWithHistory() {
    return withHistory;
  }

  /**
   * Sets the value of the withHistory property.
   *
   * @param value allowed object is
   *              {@link Boolean }
   */
  public void setWithHistory(Boolean value) {
    this.withHistory = value;
  }

}
