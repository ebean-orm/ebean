package org.tests.query.softdelete;

import io.ebean.DB;
import io.ebean.PagedList;
import io.ebean.xtest.base.TransactionalTestCase;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.onetoone.album.Cover;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSoftDeletePagingList extends TransactionalTestCase {

  @Test
  public void test() {

    List<Cover> list = new ArrayList<>();
    list.add(new Cover("SoftDelPaged-1"));
    list.add(new Cover("SoftDelPaged-2"));
    list.add(new Cover("SoftDelPaged-3"));

    DB.saveAll(list);
    DB.delete(list.get(1));

    LoggedSql.start();

    PagedList<Cover> pagedList = DB.find(Cover.class)
      .where().startsWith("s3Url", "SoftDelPaged-")
      .setMaxRows(10)
      .findPagedList();

    int totalRowCount = pagedList.getTotalCount();
    List<Cover> resultList = pagedList.getList();

    List<String> sql = LoggedSql.stop();

    assertThat(resultList).hasSize(2);
    assertThat(totalRowCount).isEqualTo(2);

    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select count(*) from cover t0 where t0.s3_url like");
    if (isPlatformBooleanNative()) {
      assertSql(sql.get(0)).contains("and t0.deleted = false; --bind(SoftDelPaged-%)");
    } else {
      assertSql(sql.get(0)).contains("and t0.deleted = 0; --bind(SoftDelPaged-%)");
    }

    assertSql(sql.get(1)).contains("where t0.s3_url like ");
    if (isPlatformBooleanNative()) {
      assertSql(sql.get(1)).contains("and t0.deleted = false");
    } else {
      assertSql(sql.get(1)).contains("and t0.deleted = 0");
    }
  }
}
