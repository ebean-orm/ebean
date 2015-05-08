package com.avaje.ebeaninternal.server.query;

import java.util.List;

import com.avaje.ebean.OrderBy;
import com.avaje.ebean.OrderBy.Property;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

/**
 * Creates the order by expression clause.
 */
public class CQueryOrderBy {

    private final BeanDescriptor<?> desc;
    
    private final SpiQuery<?> query;
    
    /**
     * Create the logical order by clause.
     */
    public static String parse(BeanDescriptor<?> desc, SpiQuery<?> query) {
        return new CQueryOrderBy(desc, query).parseInternal();
    }
    
    private CQueryOrderBy(BeanDescriptor<?> desc, SpiQuery<?> query) {
        this.desc = desc;
        this.query = query;
    }
    
    private String parseInternal() {
        
        OrderBy<?> orderBy = query.getOrderBy();
        if (orderBy == null){
            return null;
        }
        
        StringBuilder sb = new StringBuilder();

        List<Property> properties = orderBy.getProperties();
        if (properties.isEmpty()){
            // order by clause removed by filterMany()
            return null;
        }
        for (int i = 0; i < properties.size(); i++) {
            if (i > 0){
                sb.append(", ");
            }
            Property p = properties.get(i);
            String expression = parseProperty(p);
            sb.append(expression);
        }
        return sb.toString();
    }
    
    private String parseProperty(Property p) {
        
        String propName = p.getProperty();
        ElPropertyValue el  = desc.getElGetValue(propName);
        if (el == null){
            return p.toStringFormat();
        } 
        
        BeanProperty beanProperty = el.getBeanProperty();
        if (beanProperty instanceof BeanPropertyAssoc<?>){
            BeanPropertyAssoc<?> ap = (BeanPropertyAssoc<?>)beanProperty;
            IdBinder idBinder = ap.getTargetDescriptor().getIdBinder();
            return idBinder.getOrderBy(el.getElName(), p.isAscending());
        }
        
        return p.toStringFormat();
    }
}
