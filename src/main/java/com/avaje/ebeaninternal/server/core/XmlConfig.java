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
