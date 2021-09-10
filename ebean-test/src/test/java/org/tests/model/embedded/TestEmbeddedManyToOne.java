package org.tests.model.embedded;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.Property;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEmbeddedManyToOne extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    BeanType<EAddr> embType = DB.getDefault().pluginApi().beanType(EAddr.class);

    DB.getDefault().cacheManager().clearAll();

    Country nz = DB.reference(Country.class, "NZ");

    EAddr addr = new EAddr("Foo", "Bar", nz);
    EPerAddr perAddr = new EPerAddr("Embed", addr);

    Property country = embType.property("country");
    Object val = country.value(addr);

    assertThat(val).isSameAs(nz);

    DB.save(perAddr);


    LoggedSql.start();

    EPerAddr found = DB.find(EPerAddr.class, perAddr.getId());

    assertThat(found.getAddress().getCountry().getCode()).isEqualTo("NZ");
    assertThat(found.getAddress().getCountry().getName()).startsWith("New");


    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);

    if (isH2()) {
      assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version, t0.ma_street, t0.ma_suburb, t0.ma_city, t0.ma_country_code from eper_addr t0 where t0.id = ? ");
      assertSql(sql.get(1)).contains("select t0.code, t0.name from o_country t0 where t0.code = ?  ; --bind(NZ,");
    }
  }
}
