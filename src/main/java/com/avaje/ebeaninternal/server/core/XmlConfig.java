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
package com.avaje.ebeaninternal.server.core;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.lib.util.Dnode;

/**
 * Holds the orm.xml and ebean-orm.xml deployment information.
 * 
 * @author rbygrave
 */
public class XmlConfig {

    private final List<Dnode> ebeanOrmXml;
    private final List<Dnode> ormXml;
    private final List<Dnode> allXml;
    
    public XmlConfig(List<Dnode> ormXml, List<Dnode> ebeanOrmXml){
        this.ormXml = ormXml;
        this.ebeanOrmXml = ebeanOrmXml;
        this.allXml = new ArrayList<Dnode>(ormXml.size() + ebeanOrmXml.size());
        allXml.addAll(ormXml);
        allXml.addAll(ebeanOrmXml);
    }
    
    public List<Dnode> getEbeanOrmXml() {
        return ebeanOrmXml;
    }
    
    public List<Dnode> getOrmXml() {
        return ormXml;
    }

    public List<Dnode> find(List<Dnode> entityXml, String element) {
        ArrayList<Dnode> hits = new ArrayList<Dnode>();
        for (int i = 0; i < entityXml.size(); i++) {
            hits.addAll(entityXml.get(i).findAll(element, 1));
        }
        return hits;
    }
    
    /**
     * Find the deployment xml for a given entity.
     * <p>
     * This searches all the orm.xml and ebean-orm.xml files.
     * </p>
     */
    public List<Dnode> findEntityXml(String className) {

        ArrayList<Dnode> hits = new ArrayList<Dnode>(2);
        
        for (Dnode ormXml : allXml) {
            Dnode entityMappings = ormXml.find("entity-mappings");

            List<Dnode> entities = entityMappings.findAll("entity", "class", className, 1);
            if (entities.size() == 1) {
                hits.add(entities.get(0));
            }
        }

        return hits;
    }
}
