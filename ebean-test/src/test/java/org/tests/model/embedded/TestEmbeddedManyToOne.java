package org.tests.model.embedded;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.Property;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEmbeddedManyToOne extends BaseTestCase {

  @BeforeAll
  static void setup() {
    ResetBasicData.reset();
    Country nz = DB.reference(Country.class, "NZ");
    EAddr addr = new EAddr("Foo", "Bar", nz);
    DB.save(new EPerAddr("Embed", addr));
  }

  @Test
  public void test_lazyLoad() {
    BeanType<EAddr> embType = DB.getDefault().pluginApi().beanType(EAddr.class);
    DB.getDefault().cacheManager().clearAll();

    Country nz = DB.reference(Country.class, "NZ");
    EAddr addr = new EAddr("Foo", "Bar", nz);
    EPerAddr perAddr = new EPerAddr("EmbedLazy", addr);

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
      assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version, t0.ma_street, t0.ma_suburb, t0.ma_city, t0.ma_country_code from eper_addr t0 where t0.id = ?");
      assertSql(sql.get(1)).contains("select t0.code, t0.name from o_country t0 where t0.code = ?; --bind(NZ,");
    }
  }

  @Test
  void where_embeddedManyToOne_byForeignKey_noJoinNeeded() {
    LoggedSql.start();
    List<EPerAddr> found = DB.find(EPerAddr.class)
      .where().eq("address.country.code", "NZ")
      .findList();
    List<String> sql = LoggedSql.stop();

    assertThat(found).isNotEmpty();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      // correct FK column (ma_country_code) is used via the embedded prefix
      assertSql(sql.get(0)).contains("left join o_country t1 on t1.code = t0.ma_country_code");
      assertSql(sql.get(0)).contains("where t1.code = ?");
    }
  }

  @Test
  void where_embeddedManyToOne_byAssociationProperty_requiresJoin() {
    LoggedSql.start();
    List<EPerAddr> found = DB.find(EPerAddr.class)
      .where().eq("address.country.name", "New Zealand")
      .findList();
    List<String> sql = LoggedSql.stop();

    assertThat(found).isNotEmpty();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      assertSql(sql.get(0)).contains("join o_country t1 on t1.code = t0.ma_country_code");
      assertSql(sql.get(0)).contains("where t1.name = ?");
    }
  }

  @Test
  void where_embeddedManyToOne_byAssocAndFetch() {
    LoggedSql.start();
    List<EPerAddr> found = DB.find(EPerAddr.class)
      .where().eq("address.country.code", "NZ")
      .findList();
    List<String> sql = LoggedSql.stop();

    assertThat(found).isNotEmpty();
    assertThat(sql).hasSize(1);
    if (isH2()) {
      assertSql(sql.get(0)).contains("left join o_country t1 on t1.code = t0.ma_country_code");
    }
  }
}
