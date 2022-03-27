package io.ebeaninternal.server.type;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocaleParse {

  @Test
  public void test() {

    // Examples: "en", "de_DE", "_GB", "en_US_WIN", "de__POSIX", "fr__MAC"

    Locale l = parse("en");
    assertEquals("en", l.getLanguage());

    l = parse("de_DE");
    assertEquals("de", l.getLanguage());
    assertEquals("DE", l.getCountry());

    l = parse("en_US_WIN");
    assertEquals("en", l.getLanguage());
    assertEquals("US", l.getCountry());
    assertEquals("WIN", l.getVariant());

    l = parse("_GB");
    assertEquals("", l.getLanguage());
    assertEquals("GB", l.getCountry());
    assertEquals("", l.getVariant());

    l = parse("fr__MAC");
    assertEquals("fr", l.getLanguage());
    assertEquals("", l.getCountry());
    assertEquals("MAC", l.getVariant());

    l = parse("de__POSIX");
    assertEquals("de", l.getLanguage());
    assertEquals("", l.getCountry());
    assertEquals("POSIX", l.getVariant());
  }

  private Locale parse(String value) {
    return new ScalarTypeLocale().parse(value);
  }
}
