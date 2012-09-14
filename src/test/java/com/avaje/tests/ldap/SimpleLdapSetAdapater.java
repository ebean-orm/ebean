package com.avaje.tests.ldap;

import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import com.avaje.ebean.config.ldap.LdapAttributeAdapter;

public class SimpleLdapSetAdapater implements LdapAttributeAdapter {

    @SuppressWarnings("unchecked")
    public Attribute createAttribute(Object beanPropertyValue) {
        Set<Long> set = (Set<Long>)beanPropertyValue;
        
        BasicAttribute attr = new BasicAttribute("accounts");
         
        for (Long accVal : set) {
            attr.add("acct="+accVal);
        }
        
        return attr;
    }

    public Object readAttribute(Attribute attribute) throws NamingException {
        
        HashSet<Long> set = new HashSet<Long>();

        NamingEnumeration<?> all = attribute.getAll();
        while (all.hasMoreElements()) {
            String s = (String)all.nextElement();
            // just trim off the first 5 characters
            s = s.substring(5);
            set.add(new Long(s));
        }
        return set;
    }

    
}
