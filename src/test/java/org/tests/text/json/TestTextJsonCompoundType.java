package org.tests.text.json;

//import java.util.Currency;
//
//import org.junit.Assert;

import io.ebean.BaseTestCase;
import org.junit.Test;

//
//import com.avaje.ebean.Ebean;
//import com.avaje.ebean.text.json.JsonContext;
//import com.avaje.tests.model.ddd.DExhEntity;
//import com.avaje.tests.model.ddd.DPerson;
//import com.avaje.tests.model.ivo.CMoney;
//import com.avaje.tests.model.ivo.ExhangeCMoneyRate;
//import com.avaje.tests.model.ivo.Money;
//import com.avaje.tests.model.ivo.Oid;
//import com.avaje.tests.model.ivo.Rate;

public class TestTextJsonCompoundType extends BaseTestCase {

  @Test
  public void test() {

//    Currency NZD = Currency.getInstance("NZD");
//
//    DPerson p = new DPerson();
//    p.setFirstName("first");
//    p.setLastName("last");
//    p.setSalary(new Money("12200"));
//    p.setCmoney(new CMoney(new Money("12"), NZD));
//
//    JsonContext jsonContext = Ebean.createJsonContext();
//
//    String jsonString = jsonContext.toJson(p, true);
//    System.out.println(jsonString);
//
//    CMoney cm = new CMoney(new Money("12"), NZD);
//
//    Rate rate = new Rate(0.1);
//    ExhangeCMoneyRate exh = new ExhangeCMoneyRate(rate, cm);
//
//    DExhEntity ep = new DExhEntity();
//    ep.setOid(new Oid<DExhEntity>(112));
//    ep.setExhange(exh);
//
//    String jsonString0 = jsonContext.toJson(ep, true);
//    System.out.println(jsonString0);
//
//    DExhEntity bean0 = jsonContext.toBean(DExhEntity.class, jsonString0);
//    Assert.assertNotNull(bean0);

  }
}
