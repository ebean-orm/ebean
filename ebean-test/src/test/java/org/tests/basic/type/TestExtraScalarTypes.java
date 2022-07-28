package org.tests.basic.type;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ESomeType;

import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestExtraScalarTypes extends BaseTestCase {

  @Test
  void insertLargeTimezone() {
    TimeZone tz = TimeZone.getTimeZone("America/Argentina/ComodRivadavia");
    var bean = new ESomeType();
    bean.setTimeZone(tz);
    DB.save(bean);

    var found = DB.find(ESomeType.class, bean.getId());
    assert found != null;
    assertThat(found.getTimeZone()).isEqualTo(tz);

    var findByTimezone = DB.find(ESomeType.class).where().eq("timeZone", tz).findList();
    assertThat(findByTimezone).hasSize(1);
    assertThat(findByTimezone.get(0).getId()).isEqualTo(found.getId());
    assertThat(findByTimezone.get(0).getTimeZone()).isEqualTo(tz);

    DB.delete(found);
  }

  @Test
  void test() {
    Locale locale = Locale.ENGLISH;
    Currency currency = Currency.getInstance(Locale.US);
    TimeZone tz = TimeZone.getDefault();

    ESomeType e = new ESomeType();
    e.setLocale(locale);
    e.setTimeZone(tz);
    e.setCurrency(currency);

    DB.save(e);

    ESomeType e2 = DB.find(ESomeType.class).setId(e.getId()).findOne();

    assertNotNull(e2.getCurrency());
    assertNotNull(e2.getLocale());
    assertNotNull(e2.getTimeZone());

    List<ESomeType> list = DB.find(ESomeType.class)
      .where()
      .eq("locale", locale)
      .eq("timeZone", tz.getID())
      .eq("currency", currency)
      .findList();

    assertThat(list).isNotEmpty();
  }

}
