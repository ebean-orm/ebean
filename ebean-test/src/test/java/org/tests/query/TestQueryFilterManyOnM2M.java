package org.tests.query;

import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.MUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryFilterManyOnM2M extends BaseTestCase {

  @Test
  public void test() {

    LoggedSql.start();
    DB.find(MUser.class).fetch("roles").filterMany("roles").ilike("roleName", "Jim%").findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.userid, t0.user_name, t0.user_type_id, t1.roleid, t1.role_name from muser t0 left join mrole_muser t1z_ on t1z_.muser_userid = t0.userid left join mrole t1 on t1.roleid = t1z_.mrole_roleid where lower(t1.role_name) like");
    assertThat(sql.get(0)).contains("order by t0.userid;");
  }

}
