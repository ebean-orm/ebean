package com.avaje.tests.ddd.iud;

import java.util.Currency;
import java.util.List;

import junit.framework.TestCase;

import org.joda.time.Interval;
import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.tests.model.ddd.DPerson;
import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.Money;
import com.avaje.tests.model.ivo.Oid;

public class TestDPersonIUD extends TestCase {

    
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
//        p.setInterval(new Interval(System.currentTimeMillis()-20000, System.currentTimeMillis()));
//        
//        Ebean.save(p);
//        
//        Oid<DPerson> id = p.getId();
//        Assert.assertNotNull(id);
//        
//        DPerson p2 = Ebean.find(DPerson.class)
//            .setAutofetch(false)
//            .where().idEq(id)
//            .findUnique();
//        
//        Assert.assertNotNull(p2);
//        System.out.println(p2);
//        Assert.assertEquals(new Money(12200d), p2.getSalary());
//        Assert.assertNotNull(p2.getCmoney());
//        Assert.assertEquals(new Money("12"), p2.getCmoney().getAmount());
//        Assert.assertEquals(NZD, p2.getCmoney().getCurrency());
//        
//        
//        Query<DPerson> query = Ebean.find(DPerson.class)
//            .setAutofetch(false)
//            .where().gt("cmoney.amount",1)
//            .query();
//        
//        List<DPerson> list = query.findList();
//        Assert.assertTrue(list.size() >= 1);
        
        
    }
}
