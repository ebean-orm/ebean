package com.avaje.ebean;

import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class TestPropertyChangeListener extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).findList();

    Listener listener = new Listener();

    Customer customer = list.get(0);
    Ebean.getBeanState(customer).addPropertyChangeListener(listener);

    customer.setName("modName");
    customer.setSmallnote("modSmallNote");

    Assert.assertEquals(2, listener.events.size());
    Assert.assertEquals("modName", listener.events.get(0).getNewValue());
    Assert.assertEquals("name", listener.events.get(0).getPropertyName());
    Assert.assertEquals("modSmallNote", listener.events.get(1).getNewValue());
    Assert.assertEquals("smallnote", listener.events.get(1).getPropertyName());

  }

  class Listener implements PropertyChangeListener {

    List<PropertyChangeEvent> events = new ArrayList<>();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      events.add(evt);
    }

  }

}
