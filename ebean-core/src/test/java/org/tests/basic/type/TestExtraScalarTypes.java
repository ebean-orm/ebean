package org.tests.basic.type;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.ESomeType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertTrue;

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

    ESomeType e2 = Ebean.find(ESomeType.class).setAutoTune(false).setId(e.getId()).findOne();

    Assert.assertNotNull(e2.getCurrency());
    Assert.assertNotNull(e2.getLocale());
    Assert.assertNotNull(e2.getTimeZone());

    List<ESomeType> list = Ebean.find(ESomeType.class)
      .setAutoTune(false).where()
      .eq("locale", locale)
      .eq("timeZone", tz.getID())
      .eq("currency", currency)
      .findList();

    assertTrue(!list.isEmpty());
  }

}
