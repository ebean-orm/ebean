package org.tests.rawsql.nativesql;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.embedded.EAddress;
import org.tests.model.embedded.EPerson;
import org.tests.model.json.PlainBean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNativeWithEmbedded extends BaseTestCase {

  @Test
  public void test() {

    Map<String, Object> rawMap = new LinkedHashMap<>();
    rawMap.put("a","1");
    EPerson person = new EPerson();
    person.setName("Frank");
    EAddress address = new EAddress();
    address.setStreet("1 foo st");
    address.setCity("barv");
    address.setJbean(new PlainBean("hi", 3));
    address.setJraw(rawMap);
    person.setAddress(address);

    DB.save(person);

    String sql = "select id, name, street, suburb, addr_city, addr_status, addr_jbean, jraw from eperson where id = ?";

    LoggedSqlCollector.start();

    Query<EPerson> query = DB.findNative(EPerson.class, sql);
    query.setParameter(person.getId());
    EPerson one = query.findOne();

    assertThat(one.getName()).isEqualTo("Frank");
    assertThat(one.getAddress().getStreet()).isEqualTo("1 foo st");
    assertThat(one.getAddress().getJbean().getName()).isEqualTo("hi");
    assertThat(one.getAddress().getJraw().get("a")).isEqualTo("1");

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
  }
}
