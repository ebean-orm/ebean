package com.avaje.tests.ddd.iud;

import java.util.Currency;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.tests.model.ddd.DPerson;
import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.Money;

public class TestDPersonEl extends TestCase {

    public void test() {
        
        GlobalProperties.put("classes", DPerson.class.toString());
        
//        Currency NZD = Currency.getInstance("NZD");
//        
//        DPerson p = new DPerson();
//        p.setFirstName("first");
//        p.setLastName("last");
//        p.setSalary(new Money("12200"));
//        p.setCmoney(new CMoney(new Money("12"), NZD));
//        
//        SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
//        
//        BeanDescriptor<DPerson> descriptor = server.getBeanDescriptor(DPerson.class);
//        
//        ElPropertyValue elCmoney = descriptor.getElGetValue("cmoney");
//        ElPropertyValue elCmoneyAmt = descriptor.getElGetValue("cmoney.amount");
//        ElPropertyValue elCmoneyCur = descriptor.getElGetValue("cmoney.currency");
//        
//        EntityBean entityBean = (EntityBean)p;
//        
//        Object cmoney = elCmoney.elGetValue(entityBean);
//        Object amt = elCmoneyAmt.elGetValue(entityBean);
//        Object cur = elCmoneyCur.elGetValue(entityBean);
//        
//        Assert.assertNotNull(cmoney);
//        Assert.assertEquals(new Money("12"), amt);
//        Assert.assertEquals(NZD, cur);
//        
//        p.setCmoney(null);
//        Assert.assertNull(p.getCmoney());
//        
//        // won't trigger CMoney build as not all properties
//        // have been set yet...
//        elCmoneyAmt.elSetValue(entityBean, new Money("13"), true, false);
//        Assert.assertNull(p.getCmoney());
//
//        // will trigger the build and setting of CMoney
//        elCmoneyCur.elSetValue(entityBean, NZD, true, false);
//        
//        // this time not null as all required properties for
//        // the compound object have been collected
//        Assert.assertNotNull(p.getCmoney());

    }
    
}
