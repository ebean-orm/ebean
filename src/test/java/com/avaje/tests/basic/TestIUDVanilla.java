package com.avaje.tests.basic;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.EBasicVer;

public class TestIUDVanilla extends TestCase {

    public void test() {

        EBasicVer e0 = new EBasicVer();
        e0.setName("vanilla");

        Ebean.save(e0);

//        // only use the below test when not using enhancement
//        boolean entity = (e0 instanceof EntityBean);
//        Assert.assertTrue(!entity);

        Assert.assertNotNull(e0.getId());
        Assert.assertNotNull(e0.getLastUpdate());

        Timestamp lastUpdate0 = e0.getLastUpdate();

        e0.setName("modified");
        Ebean.save(e0);

        Timestamp lastUpdate1 = e0.getLastUpdate();
        Assert.assertNotNull(lastUpdate1);
        Assert.assertNotSame(lastUpdate0, lastUpdate1);

        EBasicVer e2 = Ebean.getServer(null).createEntityBean(EBasicVer.class);

        HashSet<String> loaded = new HashSet<String>();
        loaded.add("id");
        loaded.add("lastUpdate");
        loaded.add("name");

        e2.setId(e0.getId());
        e2.setLastUpdate(lastUpdate1);

        Ebean.getBeanState(e2).setLoaded(loaded);
        e2.setName("forcedUpdate");
        Ebean.save(e2);

        EBasicVer e3 = new EBasicVer();
        e3.setId(e0.getId());
        e3.setName("ModNoOCC");
        // e3.setLastUpdate(e2.getLastUpdate());

        Ebean.update(e3);
        
        e3.setName("ModAgain");
        e3.setDescription("Banana");
        
        
        Set<String> updateProps = new HashSet<String>();
        updateProps.add("name");
        updateProps.add("description");
        
        Ebean.update(e3, updateProps);

    }
}
