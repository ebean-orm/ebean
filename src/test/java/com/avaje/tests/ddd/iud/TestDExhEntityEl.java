package com.avaje.tests.ddd.iud;

import java.util.Currency;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.tests.model.ddd.DExhEntity;
import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.ExhangeCMoneyRate;
import com.avaje.tests.model.ivo.Money;
import com.avaje.tests.model.ivo.Rate;

public class TestDExhEntityEl extends TestCase {

    public void test() {
        
        GlobalProperties.put("classes", DExhEntity.class.toString());
        
        Currency NZD = Currency.getInstance("NZD");
        
        CMoney cm = new CMoney(new Money("12"), NZD);
        
        Rate rate = new Rate(0.1);
        ExhangeCMoneyRate exh = new ExhangeCMoneyRate(rate, cm);
        
        DExhEntity p = new DExhEntity();
        p.setExhange(exh);
        
        SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
        
        BeanDescriptor<DExhEntity> descriptor = server.getBeanDescriptor(DExhEntity.class);
        
        ElPropertyValue elExh = descriptor.getElGetValue("exhange");
        ElPropertyValue elExhRate = descriptor.getElGetValue("exhange.rate");
        ElPropertyValue elExhCMoney = descriptor.getElGetValue("exhange.cmoney");
        ElPropertyValue elExhCMoneyCur = descriptor.getElGetValue("exhange.cmoney.currency");
        ElPropertyValue elExhCMoneyAmt = descriptor.getElGetValue("exhange.cmoney.amount");
        
        Object e = elExh.elGetValue(p);
        Object er = elExhRate.elGetValue(p);
        Object ecm = elExhCMoney.elGetValue(p);
        Object ecmCurr = elExhCMoneyCur.elGetValue(p);
        Object ecmAmt = elExhCMoneyAmt.elGetValue(p);
        
        Assert.assertNotNull(e);
        Assert.assertNotNull(er);
        Assert.assertNotNull(ecm);
        
        Assert.assertEquals(new Rate("0.1"), er);
        Assert.assertEquals(NZD, ecmCurr);
        Assert.assertEquals(new Money("12"), ecmAmt);
        
        p.setExhange(null);
        Assert.assertNull(p.getExhange());
        
        // won't trigger CMoney build as not all properties
        // have been set yet...
        elExhCMoneyAmt.elSetValue(p, new Money("13"), true, false);
        Assert.assertNull(p.getExhange());

        elExhCMoneyCur.elSetValue(p, NZD, true, false);
        Assert.assertNull(p.getExhange());

        elExhRate.elSetValue(p, new Rate(.2), true, false);

        // this time not null as all required properties for
        // the compound object have been collected
        Assert.assertNotNull(p.getExhange());

    }
    
}
