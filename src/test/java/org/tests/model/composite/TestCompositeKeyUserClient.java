package org.tests.model.composite;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCompositeKeyUserClient extends BaseTestCase {

  @Test
  public void test() {

    CkeUser user0 = new CkeUser();
    user0.setUserPK(new CkeUserKey(20, "sally"));
    user0.setName("sally");
    Ebean.save(user0);

    CkeUser user1 = new CkeUser();
    user1.setUserPK(new CkeUserKey(20, "frank"));
    user1.setName("hello");
    Ebean.save(user1);

    CkeClient client = new CkeClient();
    client.setNotes("try it");
    client.setClientPK(new CkeClientKey(20, "susan"));
    client.setUser(user1);

    LoggedSqlCollector.start();

    Ebean.save(client);

    client.setNotes("update it");
    client.setUser(user0);

    Ebean.save(client);

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("insert into cke_client (cod_cpny, cod_client, notes, username) values (?,?,?,?)");
    assertThat(sql.get(1)).contains("update cke_client set notes=?, username=? where cod_cpny=? and cod_client=?");

    Ebean.delete(client);

  }
}
