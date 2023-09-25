package org.querytest;

import io.ebean.DB;
import org.example.domain.CaoBean;
import org.example.domain.CaoKey;
import org.example.domain.query.QCaoBean;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QueryCaoBeanTest {

  @Test
  void withEmbeddedId() {
    insertTestData();

    List<CaoBean> list = new QCaoBean()
      .key.type.eq(3)
      .findList();

    assertThat(list).hasSize(2);
    for (CaoBean caoBean : list) {
      assertThat(caoBean.getKey().type()).isEqualTo(3);
    }
  }

  private static void insertTestData() {
    var key = new CaoKey(42, 3);
    var bean = new CaoBean();
    bean.setKey(key);
    bean.setDescription("hi");

    DB.save(bean);

    var key2 = new CaoKey(43, 3);
    var bean2 = new CaoBean();
    bean2.setKey(key2);
    bean2.setDescription("hi2");

    DB.save(bean2);
  }
}
