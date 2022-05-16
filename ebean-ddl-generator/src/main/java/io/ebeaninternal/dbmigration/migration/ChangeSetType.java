package io.ebeaninternal.dbmigration.migration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for changeSetType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="changeSetType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="apply"/&gt;
 *     &lt;enumeration value="pendingDrops"/&gt;
 *     &lt;enumeration value="baseline"/&gt;
 *     &lt;enumeration value="drop"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlType(name = "changeSetType")
@XmlEnum
public enum ChangeSetType {

  @XmlEnumValue("apply")
  APPLY("apply"),
  @XmlEnumValue("pendingDrops")
  PENDING_DROPS("pendingDrops"),
  @XmlEnumValue("baseline")
  BASELINE("baseline"),
  @XmlEnumValue("drop")
  DROP("drop");
  private final String value;

  ChangeSetType(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static ChangeSetType fromValue(String v) {
    for (ChangeSetType c : ChangeSetType.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }

}
