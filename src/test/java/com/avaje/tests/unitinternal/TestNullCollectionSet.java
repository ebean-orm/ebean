package com.avaje.tests.unitinternal;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.tests.model.basic.ENullCollection;
import com.avaje.tests.model.basic.ENullCollectionDetail;

public class TestNullCollectionSet extends TestCase {

    public void test() {
        
        ENullCollection c = new ENullCollection();
        
        Ebean.save(c);
        
        ENullCollection c2 = Ebean.find(ENullCollection.class, c.getId());
        
        assertNotNull(c2);
        
        List<ENullCollectionDetail> details = c2.getDetails();
        assertNotNull(details);
        
        assertTrue("Is BeanCollection",details instanceof BeanCollection<?>);
        
        BeanCollection<?> bc = (BeanCollection<?>)details;
        assertTrue(!bc.isPopulated());
        assertNotNull(bc.getOwnerBean());
        assertNotNull(bc.getPropertyName());
    }
    
}
