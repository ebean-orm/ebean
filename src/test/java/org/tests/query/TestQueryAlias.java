package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.CKeyParent;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

public class TestQueryAlias extends BaseTestCase {

  @Test
  public void testExists() {

    ResetBasicData.reset();

    Query<CKeyParent> sq = Ebean.createQuery(CKeyParent.class)
      .select("id.oneKey").alias("st0")
      .setAutoTune(false).where().query();

    Query<CKeyParent> pq = Ebean.find(CKeyParent.class).alias("myt0").where().in("id.oneKey", sq).query();

    pq.findList();

    String sql = pq.getGeneratedSql();

    // Without alias command is should be:
    // select t0.one_key c0, t0.two_key c1, t0.name c2, t0.version c3, t0.assoc_id c4 from ckey_parent t0 where  (t0
    // .one_key) in (select t0.one_key from ckey_parent t0)
    // but with alias command SQL should look like this:
    // select myt0.one_key c0, myt0.two_key c1, myt0.name c2, myt0.version c3, myt0.assoc_id c4 from ckey_parent myt0
    // where  (myt0.one_key) in (select st0.one_key from ckey_parent st0)

    Assert.assertTrue(sql.contains("ckey_parent myt0"));
    Assert.assertTrue(sql.contains("(myt0.one_key) in (select st0.one_key from ckey_parent st0)"));
  }

  @Test
  public void testNotExists() {

    ResetBasicData.reset();

    Query<CKeyParent> sq = Ebean.createQuery(CKeyParent.class)
      .select("id.oneKey").alias("st0")
      .setAutoTune(false).where().query();

    Query<CKeyParent> pq = Ebean.find(CKeyParent.class).alias("myt0").where().notIn("id.oneKey", sq).query();

    pq.findList();

    String sql = pq.getGeneratedSql();

    // Without alias command is should be:
    // select t0.one_key c0, t0.two_key c1, t0.name c2, t0.version c3, t0.assoc_id c4 from ckey_parent t0 where  (t0
    // .one_key) in (select t0.one_key from ckey_parent t0)
    // but with alias command SQL should look like this:
    // select myt0.one_key c0, myt0.two_key c1, myt0.name c2, myt0.version c3, myt0.assoc_id c4 from ckey_parent myt0
    // where  (myt0.one_key) in (select st0.one_key from ckey_parent st0)

    Assert.assertTrue(sql.contains("ckey_parent myt0"));
    Assert.assertTrue(sql.contains("(myt0.one_key) not in (select st0.one_key from ckey_parent st0)"));
  }

}
