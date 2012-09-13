/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
