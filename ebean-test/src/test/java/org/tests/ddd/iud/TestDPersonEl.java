package org.tests.ddd.iud;

import io.ebean.DB;
import io.ebean.bean.EntityBean;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.ExpressionPath;
import io.ebean.plugin.SpiServer;
import io.ebean.text.json.JsonContext;
import org.junit.jupiter.api.Test;
import org.tests.model.ddd.DPerson;
import org.tests.model.ivo.Money;

import java.io.IOException;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestDPersonEl {

  @Test
  public void test() throws IOException {

    Currency NZD = Currency.getInstance("NZD");
    assertThat(NZD).isNotNull();

    DPerson p = new DPerson();
    p.setFirstName("first");
    p.setLastName("last");
    p.setSalary(new Money("12200"));

    DB.save(p);

    SpiServer server = DB.getDefault().pluginApi();

    BeanType<DPerson> descriptor = server.beanType(DPerson.class);


    JsonContext jsonContext = server.json();
    String json = jsonContext.toJson(p);

    DPerson bean = jsonContext.toBean(DPerson.class, json);
    assertEquals("first", bean.getFirstName());
    assertEquals(new Money("12200"), bean.getSalary());


    EntityBean entityBean = (EntityBean) p;

    ExpressionPath elSalary = descriptor.expressionPath("salary");

    Object money = elSalary.pathGet(entityBean);

    assertNotNull(money);
    assertEquals(new Money("12200"), money);

  }

}
