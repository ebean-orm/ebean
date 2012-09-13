package com.avaje.tests.unitinternal;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.tests.model.basic.EVanillaCollection;
import com.avaje.tests.model.basic.EVanillaCollectionDetail;

public class TestVanillaCollectionSet extends TestCase {

    public void test() {
        
        EVanillaCollection c = new EVanillaCollection();
        
        Ebean.save(c);
        
        EVanillaCollection c2 = Ebean.find(EVanillaCollection.class, c.getId());
        
        assertNotNull(c2);
        
        List<EVanillaCollectionDetail> details = c2.getDetails();
        assertNotNull(details);
        
        assertTrue("Is BeanCollection",details instanceof BeanCollection<?>);
        
        BeanCollection<?> bc = (BeanCollection<?>)details;
        assertTrue(!bc.isPopulated());
        assertNotNull(bc.getOwnerBean());
        assertNotNull(bc.getPropertyName());
    }
    
}
