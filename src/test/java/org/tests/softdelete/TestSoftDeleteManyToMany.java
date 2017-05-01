package org.tests.softdelete;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.softdelete.ESoftDelRole;
import org.tests.model.softdelete.ESoftDelUser;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSoftDeleteManyToMany extends BaseTestCase {

  @Test
  public void test() {

    ESoftDelRole role1 = new ESoftDelRole("role1");
    ESoftDelRole role2 = new ESoftDelRole("role2");

    Ebean.save(role1);
    Ebean.save(role2);

    ESoftDelUser user1 = new ESoftDelUser("user1");
    user1.getRoles().add(role1);
    user1.getRoles().add(role2);

    Ebean.save(user1);


    LoggedSqlCollector.start();
    Ebean.delete(user1);

    List<String> loggedSql = LoggedSqlCollector.stop();

    // No Delete from the relationship table
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("update esoft_del_user set version=?, deleted=? where id=? and version=?;");

  }

}
