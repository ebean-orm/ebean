package io.ebeaninternal.dbmigration.migration;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ddl-script complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ddl-script"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ddl" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="platforms" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ddl-script", propOrder = {
  "ddl"
})
public class DdlScript {

  @XmlElement(required = true)
  protected List<String> ddl;
  @XmlAttribute(name = "platforms")
  protected String platforms;

  /**
   * Gets the value of the ddl property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the Jakarta XML Binding object.
   * This is why there is not a <CODE>set</CODE> method for the ddl property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getDdl().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link String }
   */
  public List<String> getDdl() {
    if (ddl == null) {
      ddl = new ArrayList<>();
    }
    return this.ddl;
  }

  /**
   * Gets the value of the platforms property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getPlatforms() {
    return platforms;
  }

  /**
   * Sets the value of the platforms property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setPlatforms(String value) {
    this.platforms = value;
  }

}
