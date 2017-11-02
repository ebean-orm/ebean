package org.tests.rawsql.nativesql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.embedded.EAddress;
import org.tests.model.embedded.EPerson;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNativeWithEmbedded extends BaseTestCase {

  @Test
  public void test() {

    EPerson person = new EPerson();
    person.setName("Frank");
    EAddress address = new EAddress();
    address.setStreet("1 foo st");
    address.setCity("barv");
    person.setAddress(address);

    Ebean.save(person);

    String sql = "select id, name, street, suburb, addr_city, addr_status from eperson where id = ?";

    LoggedSqlCollector.start();

    Query<EPerson> query = Ebean.findNative(EPerson.class, sql);
    query.setParameter(1, person.getId());
    EPerson one = query.findOne();

    assertThat(one.getName()).isEqualTo("Frank");
    assertThat(one.getAddress().getStreet()).isEqualTo("1 foo st");

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
  }
}
