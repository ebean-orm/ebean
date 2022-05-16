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
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/dbmigration}changeSet" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
  "changeSet"
})
@XmlRootElement(name = "migration")
public class Migration {

  @XmlElement(required = true)
  protected List<ChangeSet> changeSet;

  /**
   * Gets the value of the changeSet property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the Jakarta XML Binding object.
   * This is why there is not a <CODE>set</CODE> method for the changeSet property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getChangeSet().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link ChangeSet }
   */
  public List<ChangeSet> getChangeSet() {
    if (changeSet == null) {
      changeSet = new ArrayList<>();
    }
    return this.changeSet;
  }

}
