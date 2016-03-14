package com.avaje.tests.model.tevent;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import org.junit.Test;

public class TestAssocOneNullTraverse extends BaseTestCase {

  @Test
  public void test() {

    TEvent event = new TEvent("event");
    Ebean.save(event);

    Ebean.find(TEvent.class)
        .fetch("one.many")
        .findList();
  }
}
