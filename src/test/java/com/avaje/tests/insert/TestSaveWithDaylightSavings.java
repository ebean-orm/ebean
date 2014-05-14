package com.avaje.tests.insert;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.EBasic;

public class TestSaveWithDaylightSavings extends BaseTestCase {

  @Test
  public void test() {

    // For it to fail, the time has to match the time at which the daylight saving changes
    // are applied in that time zone. Therefore specify it explicitly.

    TimeZone defaultTimeZone = TimeZone.getDefault();
    try {

      TimeZone.setDefault(TimeZone.getTimeZone("EET"));

      // Run the code and see how there is a 3600 second change
      Date daylightSavingDate = new Date(1351382400000l);
      // On a second run comment in the following date and see
      // how there is a 0 second change
      // daylightSavingDate = new Date(1361382400000l);

      EBasic e = new EBasic();
      e.setSomeDate(daylightSavingDate);

      Ebean.save(e);
      Assert.assertNotNull(e.getId());

      // Reload the entity from database
      EBasic e2 = Ebean.find(EBasic.class, e.getId());

      long diffMillis = e2.getSomeDate().getTime() - e.getSomeDate().getTime();

      System.out.println("The date I created " + daylightSavingDate);
      System.out.println(" --- the date i put in   : " + e.getSomeDate());
      System.out.println("          as millis      : " + e.getSomeDate().getTime());
      System.out.println(" --- the date i get back : " + e2.getSomeDate());
      System.out.println("          as millis      : " + e2.getSomeDate().getTime());
      System.out.println("The difference is " + diffMillis / 1000 + " seconds");

      Assert.assertEquals(0L, diffMillis);
      
    } finally {
      TimeZone.setDefault(defaultTimeZone);
    }

  }

}
