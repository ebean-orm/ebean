package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOnePrimaryKeyJoin extends BaseTestCase {


  private OtoPrime insert(String desc) {
    OtoPrime prime = new OtoPrime("p" + desc);
    OtoPrimeExtra extra = new OtoPrimeExtra("e" + desc);
    prime.setExtra(extra);
    Ebean.save(prime);
    return prime;
  }

  @Test
  public void insertUpdateDelete() {

    String desc = "" + System.currentTimeMillis();
    OtoPrime p1 = insert(desc);

    assertThat(p1.getExtra().getEid()).isEqualTo(p1.getPid()).as("Same id values");


    Query<OtoPrime> query = Ebean.find(OtoPrime.class).setId(p1.getPid());

    OtoPrime found = query.findOne();

    assertThat(found).isNotNull();
    assertThat(sqlOf(query, 10)).contains("select t0.pid, t0.name, t0.version, t0.pid from oto_prime t0 where t0.pid = ?")
      .as("we don't join to oto_prime_extra");

    assertThat(found.getName()).isEqualTo("p" + desc);

    Query<OtoPrime> queryWithFetch = Ebean.find(OtoPrime.class)
      .setId(p1.getPid())
      .fetch("extra");

    OtoPrime oneWith = queryWithFetch.findOne();

    assertThat(oneWith).isNotNull();
    assertThat(sqlOf(queryWithFetch, 10)).contains("select t0.pid, t0.name, t0.version, t1.eid, t1.extra, t1.version from oto_prime t0 join oto_prime_extra t1 on t1.eid = t0.pid  where t0.pid = ?")
      .as("we join to oto_prime_extra");


    assertThat(oneWith.getExtra().getExtra()).isEqualTo("e" + desc);

    thenUpdate(oneWith);
    thenDelete(found);
  }

  private void thenUpdate(OtoPrime oneWith) {

    OtoPrimeExtra extra = oneWith.getExtra();
    extra.setExtra("modified");

    Ebean.save(oneWith);

    extra.setExtra("mod2");
    oneWith.setName("mod2");

    Ebean.save(oneWith);
  }

  private void thenDelete(OtoPrime found) {


    OtoPrime bean = Ebean.find(OtoPrime.class, found.getPid());

    LoggedSqlCollector.start();
    Ebean.delete(bean);

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from oto_prime_extra where");
    assertThat(sql.get(1)).contains("delete from oto_prime where");

  }
}
