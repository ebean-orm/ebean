package org.tests.ddd.iud;

import junit.framework.TestCase;

public class TestDExhEntityEl extends TestCase {

  public void test() {

//        GlobalProperties.put("classes", DExhEntity.class.toString());

//        Currency NZD = Currency.getInstance("NZD");
//
//        CMoney cm = new CMoney(new Money("12"), NZD);
//
//        Rate rate = new Rate(0.1);
//        ExhangeCMoneyRate exh = new ExhangeCMoneyRate(rate, cm);
//
//        DExhEntity p = new DExhEntity();
//        p.setExhange(exh);
//
//        SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
//
//        BeanDescriptor<DExhEntity> descriptor = server.getBeanDescriptor(DExhEntity.class);
//
//        ElPropertyValue elExh = descriptor.getElGetValue("exhange");
//        ElPropertyValue elExhRate = descriptor.getElGetValue("exhange.rate");
//        ElPropertyValue elExhCMoney = descriptor.getElGetValue("exhange.cmoney");
//        ElPropertyValue elExhCMoneyCur = descriptor.getElGetValue("exhange.cmoney.currency");
//        ElPropertyValue elExhCMoneyAmt = descriptor.getElGetValue("exhange.cmoney.amount");
//
//        EntityBean entityBean = (EntityBean)p;
//        Object e = elExh.elGetValue(entityBean);
//        Object er = elExhRate.elGetValue(entityBean);
//        Object ecm = elExhCMoney.elGetValue(entityBean);
//        Object ecmCurr = elExhCMoneyCur.elGetValue(entityBean);
//        Object ecmAmt = elExhCMoneyAmt.elGetValue(entityBean);
//
//        Assert.assertNotNull(e);
//        Assert.assertNotNull(er);
//        Assert.assertNotNull(ecm);
//
//        Assert.assertEquals(new Rate("0.1"), er);
//        Assert.assertEquals(NZD, ecmCurr);
//        Assert.assertEquals(new Money("12"), ecmAmt);
//
//        p.setExhange(null);
//        Assert.assertNull(p.getExhange());
//
//        // won't trigger CMoney build as not all properties
//        // have been set yet...
//        elExhCMoneyAmt.elSetValue(entityBean, new Money("13"), true, false);
//        Assert.assertNull(p.getExhange());
//
//        elExhCMoneyCur.elSetValue(entityBean, NZD, true, false);
//        Assert.assertNull(p.getExhange());
//
//        elExhRate.elSetValue(entityBean, new Rate(.2), true, false);
//
//        // this time not null as all required properties for
//        // the compound object have been collected
//        Assert.assertNotNull(p.getExhange());

  }

}
