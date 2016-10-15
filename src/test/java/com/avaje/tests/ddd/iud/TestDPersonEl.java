package com.avaje.tests.ddd.iud;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.plugin.BeanType;
import com.avaje.ebean.plugin.ExpressionPath;
import com.avaje.ebean.plugin.SpiServer;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.ddd.DPerson;
import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.Money;
import org.junit.Test;

import java.io.IOException;
import java.util.Currency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestDPersonEl {

  @Test
  public void test() throws IOException {

    Currency NZD = Currency.getInstance("NZD");

    DPerson p = new DPerson();
    p.setFirstName("first");
    p.setLastName("last");
    p.setSalary(new Money("12200"));
    p.setCmoney(new CMoney(new Money("12"), NZD));

    SpiServer server = Ebean.getDefaultServer().getPluginApi();

    BeanType<DPerson> descriptor = server.getBeanType(DPerson.class);


    JsonContext jsonContext = server.json();
    String json = jsonContext.toJson(p);

    DPerson bean = jsonContext.toBean(DPerson.class, json);
    assertEquals("first", bean.getFirstName());
    assertEquals(new Money("12200"), bean.getSalary());
    assertEquals(new Money("12"), bean.getCmoney().getAmount());
    assertEquals(NZD, bean.getCmoney().getCurrency());


    EntityBean entityBean = (EntityBean) p;

    ExpressionPath elCmoney = descriptor.getExpressionPath("cmoney");
    ExpressionPath elCmoneyAmt = descriptor.getExpressionPath("cmoney.amount");
    ExpressionPath elCmoneyCur = descriptor.getExpressionPath("cmoney.currency");

    Object cmoney = elCmoney.pathGet(entityBean);
    Object amt = elCmoneyAmt.pathGet(entityBean);
    Object cur = elCmoneyCur.pathGet(entityBean);

    assertNotNull(cmoney);
    assertEquals(new Money("12"), amt);
    assertEquals(NZD, cur);

    p.setCmoney(null);
    assertNull(p.getCmoney());

  }

}
