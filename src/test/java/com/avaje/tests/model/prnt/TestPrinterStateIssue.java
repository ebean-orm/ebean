package com.avaje.tests.model.prnt;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import org.junit.Test;

public class TestPrinterStateIssue extends BaseTestCase {

  @Test
  public void test() {


    MPrinter printer = new MPrinter();
    printer.setName("foo");
    printer.setAllFlags(20L);

    Ebean.save(printer);

    MPrinterState state = new MPrinterState();
    state.setFlags(10L);
    state.setPrinter(printer);
    Ebean.save(state);

    printer.setCurrentState(state);
    Ebean.save(printer);


    MPrinter printer1 = Ebean.find(MPrinter.class, printer.getId());
    MPrinterState state1 = printer1.getCurrentState();
    state1.setFlags(9L);
    printer1.setAllFlags(99L);


//    BeanState beanState = Ebean.getBeanState(printer1);
//    Set<String> changedProps = beanState.getChangedProps();
//    System.out.println("changed: "+changedProps);

    Ebean.save(printer1);

  }
}
