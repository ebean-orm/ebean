package org.tests.model.rawsql;

import io.ebean.DB;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.OtherAggregate;
import org.tests.model.composite.CkeUser;
import org.tests.model.composite.CkeUserKey;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestRawSqlAssocOneComplexId extends BaseTestCase {

  @Test
  void test() {
    CkeUser user0 = new CkeUser();
    user0.setUserPK(new CkeUserKey(89, "OneToOne"));
    user0.setName("Other");
    DB.save(user0);

    String sql =
      "select username, cod_cpny, name, 42 as totalContacts \n" +
        "from cke_user where username = ? and cod_cpny = ?";

    RawSql rawSql = RawSqlBuilder.parse(sql)
      .columnMapping("username", "user.userPK.username")
      .columnMapping("cod_cpny", "user.userPK.codCompany")
      .columnMapping("name", "user.name")
      .create();

    List<OtherAggregate> l0 =
      DB.find(OtherAggregate.class)
        .setRawSql(rawSql)
        .setParameters("OneToOne", 89)
        .findList();

    assertThat(l0).hasSize(1);
    OtherAggregate other = l0.get(0);

    assertThat(other.getTotalContacts()).isEqualTo(42);
    assertThat(other.getUser()).isNotNull();
    assertThat(other.getUser().getUserPK().getUsername()).isEqualTo("OneToOne");
    assertThat(other.getUser().getUserPK().getCodCompany()).isEqualTo(89);
    assertThat(other.getUser().getName()).isEqualTo("Other");

    DB.delete(user0);
  }
}
