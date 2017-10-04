package io.ebeaninternal.dbmigration.migration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for anonymous complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * TODO @Rob: Can this generated automatically?
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
  "ddl"
})
@XmlRootElement(name = "ddl-script")
public class DdlScript {

  @XmlValue
  protected List<String> ddl;

  @XmlAttribute(name = "platforms")
  protected String platforms;

  /**
   * Gets the value of the value property.
   *
   * @return possible object is
   * {@link String }
   */
  public List<String> getDdl() {
    if (ddl == null) {
      ddl = new ArrayList<>();
    }
    return ddl;
  }

  public void setDdl(List<String> ddl) {
    this.ddl = ddl;
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
