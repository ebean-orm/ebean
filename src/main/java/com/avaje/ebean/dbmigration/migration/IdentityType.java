
package com.avaje.ebean.dbmigration.migration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for identityType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="identityType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="IDENTITY"/>
 *     &lt;enumeration value="SEQUENCE"/>
 *     &lt;enumeration value="GENERATOR"/>
 *     &lt;enumeration value="EXTERNAL"/>
 *     &lt;enumeration value="DEFAULT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "identityType")
@XmlEnum
public enum IdentityType {

    IDENTITY,
    SEQUENCE,
    GENERATOR,
    EXTERNAL,
    DEFAULT;

    public String value() {
        return name();
    }

    public static IdentityType fromValue(String v) {
        return valueOf(v);
    }

}
