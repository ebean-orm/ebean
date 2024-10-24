package org.tests.query.orderby;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.annotation.Platform;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.EBasicTree;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;
import org.tests.model.basic.MUserType;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestOrderByWithDistinct extends BaseTestCase {

  @Test
  public void testOrderByValidation() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .where()
      .eq("junk", "blah")
      .eq("name", "jim")
      .orderBy("id desc,path.that.does.not.exist,contacts.group.name asc").query();

    Set<String> unknownProperties = query.validate();
    assertThat(unknownProperties).isNotEmpty();
    assertThat(unknownProperties).hasSize(2);
    assertThat(unknownProperties).contains("junk", "path.that.does.not.exist");

  }

  @Test
  @IgnorePlatform({Platform.MYSQL, Platform.MARIADB, Platform.SQLSERVER, Platform.NUODB}) // do not support nulls first/last
  public void testDistinctOn() {

    MRole role = DB.reference(MRole.class, 1);

    Query<MUser> query = DB.find(MUser.class)
      .where()
      .eq("roles", role)
      .orderBy("userName asc nulls first").query();

    query.findList();

    String sql = sqlOf(query);
    if (platformDistinctOn()) {
      assertThat(sql).contains("select distinct on (t0.user_name, t0.userid) t0.userid,");
    } else if (isH2()) {
      assertThat(sql).contains("select distinct t0.userid, t0.user_name, t0.user_type_id from muser t0");
    }

  }

  @Test
  public void testOrderByOnPropWithDistinct() {
    Query<EBasicTree> query = DB.find(EBasicTree.class)
      .fetch("children")
      .where().eq("children.ref.status", EBasic.Status.ACTIVE).query();

    query.findList();
    // we expect t2.name in this query
    if (platformDistinctOn()) {
      assertSql(query).startsWith("select distinct on (t0.id, t2.name, t1.id) t0.id, t0.parent_id, t0.ref_id, t1.id, t1.parent_id, t1.ref_id, t2.name "
        + "from e_basic_tree t0 "
        + "left join e_basic_tree t1 on t1.parent_id = t0.id "
        + "join e_basic_tree u1 on u1.parent_id = t0.id "
        + "join e_basic u2 on u2.id = u1.ref_id left "
        + "join e_basic t2 on t2.id = t1.ref_id where u2.status = ? order by t0.id, t2.name");
    } else {
      assertSql(query).startsWith("select distinct t0.id, t0.parent_id, t0.ref_id, t1.id, t1.parent_id, t1.ref_id, t2.name "
        + "from e_basic_tree t0 "
        + "left join e_basic_tree t1 on t1.parent_id = t0.id "
        + "join e_basic_tree u1 on u1.parent_id = t0.id "
        + "join e_basic u2 on u2.id = u1.ref_id left "
        + "join e_basic t2 on t2.id = t1.ref_id where u2.status = ? order by t0.id, t2.name");
    }
  }

  @Test
  public void testOrderByWithDistinct() {
    Query<MUser> query = DB.find(MUser.class);
    query.findList();

    assertSql(query).doesNotContain("order by");
    assertSql(query).doesNotContain("select distinct");

    query.setMaxRows(1000);
    query.findList();
    if (isH2()) {
      assertSql(query).contains("from muser t0 limit 1000");
    }
    query = DB.find(MUser.class)
      .where()
      .eq("roles.roleName", "A")
      .query();
    query.findList();
    assertSql(query).doesNotContain("order by");
    assertSql(query).contains("select distinct");

    query.setMaxRows(1000);
    query.findList();
    if (isH2()) {
      assertSql(query).contains("where u1.role_name = ? limit 1000");
    }
  }

  @Test
  public void test() {
    /*
     * Original conversation:
     * https://groups.google.com/forum/?fromgroups=#!topic/ebean/uuvi1btdCDQ%5B1-25-false%5D
     *
     * This test exposes what may be a general problem with columns required by the order by phrase being omitted from the select.
     * I'm not sure this exposes all causes of the problem.
     */

    MUserType ut = new MUserType("md");
    DB.save(ut);
    MUser user1 = new MUser("one");
    user1.setUserType(ut);
    DB.save(user1);
    MUser user2 = new MUser("two");
    user2.setUserType(ut);
    DB.save(user2);

    MRole roleA = new MRole("A");
    DB.save(roleA);
    MRole roleB = new MRole("B");
    DB.save(roleB);

    user1.addRole(roleA);
    DB.save(user1);
    user2.addRole(roleB);
    DB.save(user2);

    Query<MUser> query = DB.find(MUser.class)
      .fetch("userType", "name")
      .where()
      .eq("roles.roleName", "A")
      .orderBy("userType.name, userName").query();
    List<MUser> list = query.findList();

    // select distinct t0.userid c0, t0.user_name c1, t1.id c2, t1.name c3
    // from muser t0
    // left join muser_type t1 on t1.id = t0.user_type_id
    // join mrole_muser u1z_ on u1z_.muser_userid = t0.userid
    // join mrole u1 on u1.roleid = u1z_.mrole_roleid
    // where u1.role_name = ?
    // order by t1.name, t0.user_name; --bind(A)

    assertEquals(1, list.size());
    assertEquals(user1, list.get(0));
    String generatedSql = query.getGeneratedSql();
    if (platformDistinctOn()) {
      assertThat(generatedSql).contains("select distinct on (t1.name, t0.user_name, t0.userid) t0.userid"); // using distinct

    } else {
      assertThat(generatedSql).contains("select distinct t0.userid"); // using distinct
    }
    assertThat(generatedSql).contains("order by t1.name,"); // name in order by
    assertThat(generatedSql).contains("t1.name");// name in select


    // repeat with slight variation, not sure this really produces a different execution path
    // this problem also manifests when autofetch eliminates properties from the select that aren't used in the objects
    // still need them to be present for purpose of order by
    // so here I'm "simulating" a scenario where autofetch has dropped userType.name
    query = DB.find(MUser.class)
      .setAutoTune(false)
      .select("userName")
      .fetch("userType", "name")
      .where()
      .eq("roles.roleName", "A")
      .orderBy("userType.name").query();
    list = query.findList();

    assertEquals(1, list.size());
    assertEquals(user1, list.get(0));

    // select distinct t0.userid c0, t0.user_name c1, t1.id c2, t1.name c3
    // from muser t0
    // left join muser_type t1 on t1.id = t0.user_type_id
    // join mrole_muser u1z_ on u1z_.muser_userid = t0.userid
    // join mrole u1 on u1.roleid = u1z_.mrole_roleid
    // where u1.role_name = ?
    // order by t1.name; --bind(A)

    generatedSql = query.getGeneratedSql();
    if (platformDistinctOn()) {
      assertThat(generatedSql).contains("select distinct on (t1.name, t0.userid) t0.userid"); // using distinct
    } else {
      assertThat(generatedSql).contains("select distinct t0.userid"); // using distinct
    }
    assertThat(generatedSql).contains("order by t1.name"); // name in order by
    assertThat(generatedSql).contains("t1.name");// name in select

  }

}
