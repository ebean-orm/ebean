package org.tests.o2m;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class TestOneToManyNoMappedBy extends BaseTestCase {

  @Test
  public void test() {

    OmAccountDBO account = new OmAccountDBO("one");
    account.getChild676().add(new OmAccountChildDBO("child0"));
    account.getChild676().add(new OmAccountChildDBO("child1"));

    account.save();

    LoggedSqlCollector.start();

    final OmAccountDBO found = DB.find(OmAccountDBO.class)
      .where().eq("name", "one")
      .findOne();

    final List<OmAccountChildDBO> children = found.getChild676();
    for (OmAccountChildDBO child : children) {
      final String description = child.getDescription();
      assertNotNull(description);
      assertSame(found, child.getBananaRama());
    }

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(1)).contains("select t0.banana_rama_id, t0.id, t0.description, t0.banana_rama_id from om_account_child_dbo t0 where");
  }
}
