package io.ebeaninternal.dbmigration.migration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for identityType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="identityType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="identity"/&gt;
 *     &lt;enumeration value="sequence"/&gt;
 *     &lt;enumeration value="generator"/&gt;
 *     &lt;enumeration value="external"/&gt;
 *     &lt;enumeration value="default"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlType(name = "identityType")
@XmlEnum
public enum IdentityType {

  @XmlEnumValue("identity")
  IDENTITY("identity"),
  @XmlEnumValue("sequence")
  SEQUENCE("sequence"),
  @XmlEnumValue("generator")
  GENERATOR("generator"),
  @XmlEnumValue("external")
  EXTERNAL("external"),
  @XmlEnumValue("default")
  DEFAULT("default");
  private final String value;

  IdentityType(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static IdentityType fromValue(String v) {
    for (IdentityType c : IdentityType.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }

}
