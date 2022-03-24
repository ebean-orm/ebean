package org.tests.softdelete;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.softdelete.ESoftDelRole;
import org.tests.model.softdelete.ESoftDelUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSoftDeleteManyToMany extends BaseTestCase {

  @Test
  public void test() {

    ESoftDelRole role1 = new ESoftDelRole("role1");
    ESoftDelRole role2 = new ESoftDelRole("role2");

    DB.save(role1);
    DB.save(role2);

    ESoftDelUser user1 = new ESoftDelUser("user1");
    user1.getRoles().add(role1);
    user1.getRoles().add(role2);

    DB.save(user1);


    LoggedSql.start();
    DB.delete(user1);

    List<String> loggedSql = LoggedSql.stop();

    // No Delete from the relationship table
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("update esoft_del_user set version=?, deleted=? where id=? and version=?;");

  }

}
