package org.tests.model.composite;

import io.ebean.UnmodifiableEntityException;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestCompositeKeyUserClient extends BaseTestCase {

  @Test
  void test() {

    CkeUser user0 = new CkeUser();
    user0.setUserPK(new CkeUserKey(20, "sally"));
    user0.setName("sally");
    DB.save(user0);

    CkeUser user1 = new CkeUser();
    user1.setUserPK(new CkeUserKey(20, "frank"));
    user1.setName("hello");
    DB.save(user1);

    CkeClient client = new CkeClient();
    client.setNotes("try it");
    client.setClientPK(new CkeClientKey(20, "susan"));
    client.setUser(user1);

    List<CkeUser> unmodifiable = DB.find(CkeUser.class)
      .setUnmodifiable(true)
      .where().eq("userPK.codCompany", 20)
      .findList();

    assertThat(unmodifiable).hasSize(2);
    for (CkeUser ckeUser : unmodifiable) {
      CkeUserKey userPK = ckeUser.getUserPK();
      assertThatThrownBy(() -> userPK.setCodCompany(7))
        .describedAs("EmbeddedId is unmodifiable")
        .isInstanceOf(UnmodifiableEntityException.class);
    }

    LoggedSql.start();

    DB.save(client);

    client.setNotes("update it");
    client.setUser(user0);

    DB.save(client);

    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("insert into cke_client (cod_cpny, cod_client, notes, username) values (?,?,?,?)");
    assertSql(sql.get(1)).contains("update cke_client set notes=?, username=? where cod_cpny=? and cod_client=?");

    DB.delete(client);

  }
}
