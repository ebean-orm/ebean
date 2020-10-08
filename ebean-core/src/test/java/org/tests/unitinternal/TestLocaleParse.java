package org.tests.unitinternal;

import io.ebeaninternal.server.type.ScalarTypeLocale;
import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public class TestLocaleParse {

  @Test
  public void test() {

    // Examples: "en", "de_DE", "_GB", "en_US_WIN", "de__POSIX", "fr__MAC"

    Locale l = parse("en");
    Assert.assertEquals("en", l.getLanguage());

    l = parse("de_DE");
    Assert.assertEquals("de", l.getLanguage());
    Assert.assertEquals("DE", l.getCountry());

    l = parse("en_US_WIN");
    Assert.assertEquals("en", l.getLanguage());
    Assert.assertEquals("US", l.getCountry());
    Assert.assertEquals("WIN", l.getVariant());

    l = parse("_GB");
    Assert.assertEquals("", l.getLanguage());
    Assert.assertEquals("GB", l.getCountry());
    Assert.assertEquals("", l.getVariant());

    l = parse("fr__MAC");
    Assert.assertEquals("fr", l.getLanguage());
    Assert.assertEquals("", l.getCountry());
    Assert.assertEquals("MAC", l.getVariant());

    l = parse("de__POSIX");
    Assert.assertEquals("de", l.getLanguage());
    Assert.assertEquals("", l.getCountry());
    Assert.assertEquals("POSIX", l.getVariant());
  }

  private Locale parse(String value) {

    ScalarTypeLocale st = new ScalarTypeLocale();
    return (Locale) st.parse(value);
  }
}
