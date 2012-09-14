package com.avaje.ebeaninternal.server.type;


/**
 * ScalarType for LDAP Boolean.
 */
public class ScalarTypeLdapBoolean extends ScalarTypeBoolean.StringBoolean {

    public ScalarTypeLdapBoolean() {
        super("TRUE", "FALSE");
    }

}
