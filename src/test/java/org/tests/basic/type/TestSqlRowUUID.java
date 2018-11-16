package org.tests.basic.type;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import org.junit.Test;
import org.tests.model.basic.TUuidEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestSqlRowUUID extends BaseTestCase {

  @Test
  public void test() {

    TUuidEntity e = new TUuidEntity();
    e.setName("blah");

    Ebean.save(e);

    SqlQuery q = Ebean.createSqlQuery("select * from tuuid_entity where id = :id");
    q.setParameter("id", e.getId());
    SqlRow sqlRow = q.findOne();

    UUID id = sqlRow.getUUID("id");

    assertNotNull(id);

    Boolean b = sqlRow.getBoolean("name");
    assertFalse(b);


    SqlQuery q2 = Ebean.createSqlQuery("select id from tuuid_entity where id = :id");
    q2.setParameter("id", e.getId());
    UUID value = q2.findSingleAttribute(UUID.class);

    assertThat(value).isEqualTo(e.getId());

    if (isPostgres()) {
      usingPostrgesAnyWithPositionedParameter_needsExplicitCast(e);
    }
  }

  private void usingPostrgesAnyWithPositionedParameter_needsExplicitCast(TUuidEntity e) {

    List<UUID> ids = Arrays.asList(e.getId(), UUID.randomUUID());

    List<SqlRow> result = Ebean.createSqlQuery("select id from tuuid_entity where id = any(?::uuid[])")
      .setParameter(1, ids)
      .findList();

    assertThat(result).hasSize(1);
  }
}
