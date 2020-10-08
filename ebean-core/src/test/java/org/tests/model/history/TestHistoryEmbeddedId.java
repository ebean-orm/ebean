package org.tests.model.history;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.ValuePair;
import io.ebean.Version;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class TestHistoryEmbeddedId extends BaseTestCase {

  @Test
  @ForPlatform({Platform.H2, Platform.POSTGRES})
  public void findVersions_when_embeddedId() throws InterruptedException {

    HEmbiBean bean = new HEmbiBean(new HEmbiId(10,"ten"), "ten");
    bean.save();

    Thread.sleep(10);
    bean.setName("notTen");
    bean.save();

    HEmbiId id = new HEmbiId(10,"ten");
    final HEmbiBean found = DB.find(HEmbiBean.class, id);
    assertNotNull(found);
    assertThat(found.getName()).isEqualTo("notTen");

    LoggedSqlCollector.start();
    final List<Version<HEmbiBean>> versions =
      DB.find(HEmbiBean.class)
      .setId(id)
      .findVersions();

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(versions).hasSize(2);

    final Map<String, ValuePair> diff = versions.get(0).getDiff();
    final ValuePair namePair = diff.get("name");
    assertThat(namePair.getNewValue()).isEqualTo("notTen");
    assertThat(namePair.getOldValue()).isEqualTo("ten");

    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("from hembi_bean_with_history t0 where t0.part = ?  and t0.brand = ?");
    if (isH2()) {
      assertThat(sql.get(0)).contains("order by t0.sys_period_start desc");
    } else if (isPostgres()) {
      assertThat(sql.get(0)).contains("order by lower(t0.sys_period) desc");
    }
  }
}
