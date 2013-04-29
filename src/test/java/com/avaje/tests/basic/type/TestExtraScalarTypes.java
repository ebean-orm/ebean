package com.avaje.tests.basic.type;

import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.ESomeType;

public class TestExtraScalarTypes extends BaseTestCase {

  @Test
  public void test() {

    Locale locale = Locale.getDefault();
    Currency currency = Currency.getInstance(locale);
    TimeZone tz = TimeZone.getDefault();

    ESomeType e = new ESomeType();
    e.setLocale(locale);
    e.setTimeZone(tz);
    e.setCurrency(currency);

    Ebean.save(e);

    ESomeType e2 = Ebean.find(ESomeType.class).setAutofetch(false).setId(e.getId()).findUnique();

    Assert.assertNotNull(e2.getCurrency());
    Assert.assertNotNull(e2.getLocale());
    Assert.assertNotNull(e2.getTimeZone());
  }

}
