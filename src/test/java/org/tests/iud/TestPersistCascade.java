package org.tests.iud;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import org.avaje.moduuid.ModUUID;
import org.junit.Test;

public class TestPersistCascade extends BaseTestCase {

  @Test
  public void insert() throws InterruptedException {

    PcfCountry country = new PcfCountry();
    for (int a = 0; a < 3; a++) {
      PcfPerson mayor = createPerson();
      PcfPerson viceMayor = createPerson();
      country.addCity(new PcfCity("c_" + ModUUID.newShortId(), mayor, viceMayor));
    }
//    try (Transaction txn = DB.beginTransaction()) {
//      txn.setBatchSize(20);
//      country.save();
//      txn.commit();
//    }

    country.save();


    Thread.sleep(2000);

    country.deletePermanent();
  }

  private static PcfPerson createPerson() {
    PcfPerson person = new PcfPerson("per_" + ModUUID.newShortId());
    for (int a = 0; a < 2; a++) {
      PcfCalendar calendar = new PcfCalendar();
      for (int b = 0; b < 10; b++) {
        calendar.addEvent(new PcfEvent("evt_" + ModUUID.newShortId()));
      }
      person.addCalendar(calendar);
    }
    return person;
  }

}
