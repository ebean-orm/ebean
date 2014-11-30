package com.avaje.tests.ddd.iud;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.tests.model.ddd.DPerson;
import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.Money;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;
import java.util.Currency;

public class TestDPersonEl extends TestCase {

    public void test() throws IOException {

        Currency NZD = Currency.getInstance("NZD");
        
        DPerson p = new DPerson();
        p.setFirstName("first");
        p.setLastName("last");
        p.setSalary(new Money("12200"));
        p.setCmoney(new CMoney(new Money("12"), NZD));
        
        SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
        
        BeanDescriptor<DPerson> descriptor = server.getBeanDescriptor(DPerson.class);
        
        ElPropertyValue elCmoney = descriptor.getElGetValue("cmoney");
//        ElPropertyValue elCmoneyAmt = descriptor.getElGetValue("cmoney.amount");
//        ElPropertyValue elCmoneyCur = descriptor.getElGetValue("cmoney.currency");
        
        JsonContext jsonContext = server.json();
        String json = jsonContext.toJson(p);
        
        DPerson bean = jsonContext.toBean(DPerson.class, json);
        Assert.assertEquals("first", bean.getFirstName());
        Assert.assertEquals(new Money("12200"), bean.getSalary());
        Assert.assertEquals(new Money("12"), bean.getCmoney().getAmount());
        Assert.assertEquals(NZD, bean.getCmoney().getCurrency());
        
        
        EntityBean entityBean = (EntityBean)p;
        
        Object cmoney = elCmoney.elGetValue(entityBean);
//        Object amt = elCmoneyAmt.elGetValue(entityBean);
//        Object cur = elCmoneyCur.elGetValue(entityBean);
        
        Assert.assertNotNull(cmoney);
//        Assert.assertEquals(new Money("12"), amt);
//        Assert.assertEquals(NZD, cur);
        
        p.setCmoney(null);
        Assert.assertNull(p.getCmoney());

    }
    
}
