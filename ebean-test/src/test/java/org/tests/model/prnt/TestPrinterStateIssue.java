package org.tests.model.prnt;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

public class TestPrinterStateIssue extends BaseTestCase {

  @Test
  public void test() {

    MSomeOther other = new MSomeOther();
    other.setName("otherRefBean");
    DB.save(other);

    MPrinter printer = new MPrinter();
    printer.setDataWarehouseId(other);
    printer.setName("foo");
    printer.setAllFlags(20L);

    DB.save(printer);

    MPrinterState state = new MPrinterState();
    state.setDataWarehouseId(other);
    state.setFlags(10L);
    state.setPrinter(printer);
    DB.save(state);

    MPrinterState cyanState = new MPrinterState();
    cyanState.setDataWarehouseId(other);
    cyanState.setFlags(10L);
    cyanState.setPrinter(printer);
    DB.save(state);

    printer.setCurrentState(state);
    printer.setLastTonerSwapCyan(cyanState);
    DB.save(printer);

    MPrinterState newState = new MPrinterState();
    newState.setDataWarehouseId(other);
    newState.setFlags(10L);
    //DB.save(newState);

    printer.setLastTonerSwapCyan(newState);
    newState.setPrinter(printer);

    DB.save(printer);

    MPrinter printer1 = DB.find(MPrinter.class, printer.getId());
    MPrinterState state1 = printer1.getCurrentState();
    state1.setFlags(9L);
    printer1.setAllFlags(99L);

    state1.setFlags(7L);


//    BeanState beanState = DB.beanState(printer1);
//    Set<String> changedProps = beanState.getChangedProps();
//    System.out.println("changed: "+changedProps);

    DB.save(printer1);

  }
}
