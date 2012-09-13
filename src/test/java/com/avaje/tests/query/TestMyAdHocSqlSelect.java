package com.avaje.tests.query;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.MyAdHoc;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestMyAdHocSqlSelect extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        List<MyAdHoc> list = Ebean.find(MyAdHoc.class)
            .where().gt("order_id", 0)
            .having().gt("detailCount", 0)
            .findList();
        
        Assert.assertNotNull(list);
    }
    
}
