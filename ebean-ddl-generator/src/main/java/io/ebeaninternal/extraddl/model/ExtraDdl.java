package io.ebeaninternal.extraddl.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/extraddl}ddl-script" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
  "ddlScript"
})
@XmlRootElement(name = "extra-ddl")
public class ExtraDdl {

  @XmlElement(name = "ddl-script", required = true)
  protected List<DdlScript> ddlScript;

  /**
   * Gets the value of the ddlScript property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the ddlScript property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getDdlScript().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link DdlScript }
   */
  public List<DdlScript> getDdlScript() {
    if (ddlScript == null) {
      ddlScript = new ArrayList<>();
    }
    return this.ddlScript;
  }

}
