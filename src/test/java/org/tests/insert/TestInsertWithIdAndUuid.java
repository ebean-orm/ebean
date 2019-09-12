package org.tests.insert;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInsertWithIdAndUuid extends BaseTestCase {

  @Test
  public void insert() {

    EIdUidBean bean = new EIdUidBean("one");
    bean.save();

    assertThat(bean.getId()).isGreaterThan(0);
    assertThat(bean.getUuid()).isNotNull();

    final EIdUidBean found = DB.find(EIdUidBean.class, bean.getId());

    assertThat(bean.getId()).isEqualTo(found.getId());
    assertThat(bean.getUuid()).isEqualTo(found.getUuid());
  }
}
