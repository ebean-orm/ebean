
package com.avaje.ebean.dbmigration.migration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for changeSetType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="changeSetType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="apply"/>
 *     &lt;enumeration value="drop"/>
 *     &lt;enumeration value="baseline"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "changeSetType")
@XmlEnum
public enum ChangeSetType {

    @XmlEnumValue("apply")
    APPLY("apply"),
    @XmlEnumValue("drop")
    DROP("drop"),
    @XmlEnumValue("baseline")
    BASELINE("baseline");
    private final String value;

    ChangeSetType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ChangeSetType fromValue(String v) {
        for (ChangeSetType c: ChangeSetType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
