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

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class MockDirContext implements DirContext {

    public void bind(Name name, Object obj, Attributes attrs) throws NamingException {
        
    }

    public void bind(String name, Object obj, Attributes attrs) throws NamingException {
        
    }

    public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {
        return null;
    }

    public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
        return null;
    }

    public Attributes getAttributes(Name name, String[] attrIds) throws NamingException {
        BasicAttributes a = new BasicAttributes();
        a.put("uid", "rbygraveTest01");
        return a;
    }

    public Attributes getAttributes(Name name) throws NamingException {
        return null;
    }

    public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
        return null;
    }

    public Attributes getAttributes(String name) throws NamingException {
        return null;
    }

    public DirContext getSchema(Name name) throws NamingException {
        return null;
    }

    public DirContext getSchema(String name) throws NamingException {
        return null;
    }

    public DirContext getSchemaClassDefinition(Name name) throws NamingException {
        return null;
    }

    public DirContext getSchemaClassDefinition(String name) throws NamingException {
        return null;
    }

    public void modifyAttributes(Name name, int modOp, Attributes attrs) throws NamingException {
        
    }

    public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException {
        
    }

    public void modifyAttributes(String name, int modOp, Attributes attrs) throws NamingException {
        
    }

    public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
        
    }

    public void rebind(Name name, Object obj, Attributes attrs) throws NamingException {
        
    }

    public void rebind(String name, Object obj, Attributes attrs) throws NamingException {
        
    }

    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn)
            throws NamingException {
        return null;
    }

    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes) throws NamingException {
        return null;
    }

    public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs, SearchControls cons)
            throws NamingException {
        return null;
    }

    public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons) throws NamingException {
        return null;
    }

    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes,
            String[] attributesToReturn) throws NamingException {
        return null;
    }

    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) throws NamingException {
        return null;
    }

    public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs,
            SearchControls cons) throws NamingException {
        return null;
    }

    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
            throws NamingException {
        return null;
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return null;
    }

    public void bind(Name name, Object obj) throws NamingException {
        
    }

    public void bind(String name, Object obj) throws NamingException {
        
    }

    public void close() throws NamingException {
        
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        return null;
    }

    public String composeName(String name, String prefix) throws NamingException {
        return null;
    }

    public Context createSubcontext(Name name) throws NamingException {
        return null;
    }

    public Context createSubcontext(String name) throws NamingException {
        return null;
    }

    public void destroySubcontext(Name name) throws NamingException {
        
    }

    public void destroySubcontext(String name) throws NamingException {
        
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return null;
    }

    public String getNameInNamespace() throws NamingException {
        return null;
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return null;
    }

    public NameParser getNameParser(String name) throws NamingException {
        return null;
    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return null;
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return null;
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return null;
    }

    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return null;
    }

    public Object lookup(Name name) throws NamingException {
        return null;
    }

    public Object lookup(String name) throws NamingException {
        return null;
    }

    public Object lookupLink(Name name) throws NamingException {
        return null;
    }

    public Object lookupLink(String name) throws NamingException {
        return null;
    }

    public void rebind(Name name, Object obj) throws NamingException {
        
    }

    public void rebind(String name, Object obj) throws NamingException {
        
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        return null;
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        
    }

    public void rename(String oldName, String newName) throws NamingException {
        
    }

    public void unbind(Name name) throws NamingException {
        
    }

    public void unbind(String name) throws NamingException {
        
    }

}
