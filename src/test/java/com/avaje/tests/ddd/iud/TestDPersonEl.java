package com.avaje.tests.ddd.iud;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.plugin.BeanType;
import com.avaje.ebean.plugin.ExpressionPath;
import com.avaje.ebean.plugin.SpiServer;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.ddd.DPerson;
import com.avaje.tests.model.ivo.Money;
import org.junit.Test;

import java.io.IOException;
import java.util.Currency;

import static org.junit.Assert.*;

public class TestDPersonEl {

  @Test
  public void test() throws IOException {

    Currency NZD = Currency.getInstance("NZD");

    DPerson p = new DPerson();
    p.setFirstName("first");
    p.setLastName("last");
    p.setSalary(new Money("12200"));

    Ebean.save(p);

    SpiServer server = Ebean.getDefaultServer().getPluginApi();

    BeanType<DPerson> descriptor = server.getBeanType(DPerson.class);


    JsonContext jsonContext = server.json();
    String json = jsonContext.toJson(p);

    DPerson bean = jsonContext.toBean(DPerson.class, json);
    assertEquals("first", bean.getFirstName());
    assertEquals(new Money("12200"), bean.getSalary());


    EntityBean entityBean = (EntityBean) p;

    ExpressionPath elSalary = descriptor.getExpressionPath("salary");

    Object money = elSalary.pathGet(entityBean);

    assertNotNull(money);
    assertEquals(new Money("12200"), money);

  }

}
