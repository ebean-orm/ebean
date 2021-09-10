package org.tests.merge;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.MergeOptions;
import io.ebean.MergeOptionsBuilder;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.UUOne;
import org.tests.model.basic.UUTwo;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMergeBasic extends BaseTestCase {


  private UUOne rebuildViaJson(UUOne input) {
    String asJson = DB.json().toJson(input);
    return DB.json().toBean(UUOne.class, asJson);
  }

  private UUOne build() {
    UUOne uuOne = buildGraph();
    DB.save(uuOne);
    return rebuildViaJson(uuOne);
  }

  @Test
  public void with_setClientGeneratedIds_expect_noDifferenceWithToMany() {

    UUOne one = build();

    one.setDescription("mod");
    List<UUTwo> comments = one.getComments();
    comments.remove(0);
    comments.add(0, new UUTwo("twoMore", UUID.randomUUID()));
    comments.add(new UUTwo("twoExtra", UUID.randomUUID()));

    MergeOptions options = new MergeOptionsBuilder()
      .addPath("comments")
      .setClientGeneratedIds()
      .build();

    LoggedSql.start();
    DB.merge(one, options);


    List<String> sql = LoggedSql.stop();
    // fetch the Ids ... used to identity inserts, updates and deletes
    assertSql(sql.get(0)).contains("select t0.id, t1.id from uuone t0 left join uutwo t1 on t1.master_id = t0.id where t0.id = ?");

    // deletes of Ids that are no longer in the graph
    assertSql(sql.get(1)).contains("delete from uutwo where id=?");

    // cascade persist ... master
    assertSql(sql.get(2)).contains("update uuone set name=?, description=?, version=? where id=? and version=?");

    // persist children ...
    if (isPersistBatchOnCascade()) {
      assertThat(sql.get(3)).contains("insert into uutwo (id, name, notes, version, master_id) values (?,?,?,?,?)");
      assertThat(sql.get(4)).contains("-- bind(");
      assertThat(sql.get(5)).contains("-- bind(");
      assertThat(sql.get(6)).contains("update uutwo set name=?, notes=?, version=?, master_id=? where id=? and version=?");
      assertThat(sql.get(7)).contains("-- bind(");
      assertThat(sql.get(8)).contains("-- bind(");
      assertThat(sql.get(9)).contains("-- bind(");

    } else {
      assertThat(sql.get(3)).contains("insert into uutwo (id, name, notes, version, master_id) values (?,?,?,?,?);");
      assertThat(sql.get(4)).contains("update uutwo set name=?, notes=?, version=?, master_id=? where id=? and version=?");
      assertThat(sql.get(5)).contains("update uutwo set name=?, notes=?, version=?, master_id=? where id=? and version=?");
      assertThat(sql.get(6)).contains("update uutwo set name=?, notes=?, version=?, master_id=? where id=? and version=?");
      assertThat(sql.get(7)).contains("insert into uutwo (id, name, notes, version, master_id) values (?,?,?,?,?);");
    }
  }

  @Test
  public void test() {

    UUOne one = build();
    one.setDescription("mod");

    List<UUTwo> comments = one.getComments();
    comments.remove(0);
    comments.add(0, new UUTwo("twoMore", UUID.randomUUID()));
    comments.add(new UUTwo("twoExtra", UUID.randomUUID()));

    MergeOptions options = new MergeOptionsBuilder()
      .addPath("comments")
      .build();

    LoggedSql.start();
    DB.merge(one, options);


    List<String> sql = LoggedSql.stop();
    // fetch the Ids ... used to identity inserts, updates and deletes
    assertSql(sql.get(0)).contains("select t0.id, t1.id from uuone t0 left join uutwo t1 on t1.master_id = t0.id where t0.id = ?");

    // deletes of Ids that are no longer in the graph
    assertSql(sql.get(1)).contains("delete from uutwo where id=?");

    // cascade persist ... master
    assertSql(sql.get(2)).contains("update uuone set name=?, description=?, version=? where id=? and version=?");

    // persist children ...
    if (isPersistBatchOnCascade()) {
      assertThat(sql.get(3)).contains("insert into uutwo (id, name, notes, version, master_id) values (?,?,?,?,?)");
      assertThat(sql.get(4)).contains("-- bind(");
      assertThat(sql.get(5)).contains("-- bind(");
      assertThat(sql.get(6)).contains("update uutwo set name=?, notes=?, version=?, master_id=? where id=? and version=?");
      assertThat(sql.get(9)).contains("-- bind(");

    } else {
      assertThat(sql.get(3)).contains("insert into uutwo (id, name, notes, version, master_id) values (?,?,?,?,?);");
      assertThat(sql.get(6)).contains("update uutwo set name=?, notes=?, version=?, master_id=? where id=? and version=?");
    }
  }

  private UUOne buildGraph() {
    UUOne one = new UUOne("one1", UUID.randomUUID());
    for (int i = 1; i < 5; i++) {
      UUTwo two = new UUTwo("two" + i, UUID.randomUUID());
      one.getComments().add(two);
    }
    return one;
  }
}
