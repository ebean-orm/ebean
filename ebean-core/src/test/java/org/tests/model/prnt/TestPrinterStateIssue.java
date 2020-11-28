package org.tests.model.prnt;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

public class TestPrinterStateIssue extends BaseTestCase {

  @Test
  public void test() {

    MSomeOther other = new MSomeOther();
    other.setName("otherRefBean");
    Ebean.save(other);

    MPrinter printer = new MPrinter();
    printer.setDataWarehouseId(other);
    printer.setName("foo");
    printer.setAllFlags(20L);

    Ebean.save(printer);

    MPrinterState state = new MPrinterState();
    state.setDataWarehouseId(other);
    state.setFlags(10L);
    state.setPrinter(printer);
    Ebean.save(state);

    MPrinterState cyanState = new MPrinterState();
    cyanState.setDataWarehouseId(other);
    cyanState.setFlags(10L);
    cyanState.setPrinter(printer);
    Ebean.save(state);

    printer.setCurrentState(state);
    printer.setLastTonerSwapCyan(cyanState);
    Ebean.save(printer);

    MPrinterState newState = new MPrinterState();
    newState.setDataWarehouseId(other);
    newState.setFlags(10L);
    //Ebean.save(newState);

    printer.setLastTonerSwapCyan(newState);
    newState.setPrinter(printer);

    Ebean.save(printer);

    MPrinter printer1 = Ebean.find(MPrinter.class, printer.getId());
    MPrinterState state1 = printer1.getCurrentState();
    state1.setFlags(9L);
    printer1.setAllFlags(99L);

    state1.setFlags(7L);


//    BeanState beanState = Ebean.getBeanState(printer1);
//    Set<String> changedProps = beanState.getChangedProps();
//    System.out.println("changed: "+changedProps);

    Ebean.save(printer1);

  }
}
