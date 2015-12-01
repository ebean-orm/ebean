package com.avaje.tests.softdelete;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.softdelete.EBasicSoftDelete;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSoftDeleteBasic extends BaseTestCase {

  @Test
  public void test() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("one");

    Ebean.save(bean);

    Ebean.delete(bean);

    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from ebasic_soft_delete where id=?");
    sqlQuery.setParameter(1, bean.getId());
    SqlRow sqlRow = sqlQuery.findUnique();
    assertThat(sqlRow).isNotNull();

    EBasicSoftDelete findNormal = Ebean.find(EBasicSoftDelete.class)
        .setId(bean.getId())
        .findUnique();

    assertThat(findNormal).isNull();

    EBasicSoftDelete findInclude = Ebean.find(EBasicSoftDelete.class)
        .setId(bean.getId())
        .includeSoftDeletes()
        .findUnique();

    assertThat(findInclude).isNotNull();

  }
}
