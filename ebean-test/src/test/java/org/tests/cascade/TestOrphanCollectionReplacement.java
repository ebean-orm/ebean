package org.tests.cascade;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

class TestOrphanCollectionReplacement extends BaseTestCase {

  @Test
  void replaceCollection_whenOrphan_expect_forcedInsertWithStatement() {
    long parentId = setup(1000); // can be handled by statement for SqlServer
    List<String> sql = doUpdate(parentId, name -> !"c0".equals(name));
    if (isH2() || isPostgresCompatible()) { // using deleted=true vs deleted=1
      assertThat(sql).hasSize(6);
      assertThat(sql.get(0)).contains("update coone_many set deleted=true where coone_id = ? and not ( id ");
      assertThat(sql.get(1)).contains(" -- bind(");
      assertThat(sql.get(3)).contains("insert into coone_many (coone_id, name, deleted) values (?,?,?)");
      assertThat(sql.get(4)).contains(" -- bind(");
    }

    if (isSqlServer()) {
      // statement mode
      assertThat(sql).hasSize(6);
      assertThat(sql.get(0)).contains("update coone_many set deleted=1 where coone_id = ? and not ( id ");
      assertThat(sql.get(1)).contains(" -- bind(");
      assertThat(sql.get(3)).contains("insert into coone_many (id, coone_id, name, deleted) values (?,?,?,?)");
      assertThat(sql.get(4)).contains(" -- bind(");
    }
    COOne fetchedUser2 = DB.find(COOne.class, parentId);
    requireNonNull(fetchedUser2);
    assertThat(fetchedUser2.getChildren())
      .hasSize(1000)
      .extracting(COOneMany::getName)
      .doesNotContain("c0")// filtered
      .contains("c1")
      .contains("cTest"); // added
  }

  @Test
  void replaceCollection_whenOrphan_expect_forcedInsertWithFilter() {
    long parentId = setup(2500); // we cannot make a "not in" query for so many params
    List<String> sql = doUpdate(parentId, name -> !"c0".equals(name));
    if (isH2() || isPostgresCompatible()) { // using deleted=true vs deleted=1
      // CHECKME: H2 would not require the batch mode here and could theoretically do it in fewer statements
      assertThat(sql).hasSize(7);
      assertThat(sql.get(0)).contains("select t0.id from coone_many t0 where coone_id=? and t0.deleted = false and t0.deleted = false; --bind");
      if (isH2()) {
        assertThat(sql.get(1)).contains("update coone_many set deleted=true where id  in (?)");
      }
      assertThat(sql.get(2)).contains(" -- bind(");
      assertThat(sql.get(4)).contains("insert into coone_many (coone_id, name, deleted) values (?,?,?)");
      assertThat(sql.get(5)).contains(" -- bind(");
    }

    if (isSqlServer()) {
      // filter mode
      assertThat(sql).hasSize(7);
      assertThat(sql.get(0)).contains("select t0.id from coone_many t0 where coone_id=? and t0.deleted = 0 and t0.deleted = 0; --bind");
      assertThat(sql.get(1)).contains("update coone_many set deleted=1 where id  in (?)");
      assertThat(sql.get(2)).contains(" -- bind(");
      assertThat(sql.get(4)).contains("insert into coone_many (id, coone_id, name, deleted) values (?,?,?,?)");
      assertThat(sql.get(5)).contains(" -- bind(");
    }
    COOne fetchedUser2 = DB.find(COOne.class, parentId);
    requireNonNull(fetchedUser2);
    assertThat(fetchedUser2.getChildren())
      .hasSize(2500)
      .extracting(COOneMany::getName)
      .doesNotContain("c0")// filtered
      .contains("c1")
      .contains("cTest"); // added
  }

  @Test
  void replaceCollection_whenOrphan_expect_forcedInsertWithManyReplacement() {
    long parentId = setup(5000); //  we will replace 2500 beans in this step
    List<String> sql = doUpdate(parentId, name -> Integer.parseInt(name.substring(1)) >= 2500);
    if (isH2()) { // using deleted=true vs deleted=1
      // CHECKME: H2 would not require the batch mode here and could theoretically do it in fewer statements
      assertThat(sql).hasSize(11);
      assertThat(sql.get(0)).contains("select t0.id from coone_many t0 where coone_id=? and t0.deleted = false and t0.deleted = false; --bind"); // find all Ids
      assertThat(sql.get(1)).contains("update coone_many set deleted=true where id  in (?,?,?");
      assertThat(sql.get(2)).contains(" -- bind(Array[1000]="); // update first 1000
      assertThat(sql.get(3)).contains(" -- bind(Array[1000]="); // update second 1000
      assertThat(sql.get(4)).contains("update coone_many set deleted=true where id  in (?,?,?");
      assertThat(sql.get(5)).contains(" -- bind(Array[500]="); // update last 500
      assertThat(sql.get(8)).contains("insert into coone_many (coone_id, name, deleted) values (?,?,?)");
      assertThat(sql.get(9)).contains(" -- bind(");
    }
    if (isPostgresCompatible()) {
      assertThat(sql).hasSize(9);
      assertThat(sql.get(0)).contains("select t0.id from coone_many t0 where coone_id=? and t0.deleted = false and t0.deleted = false; --bind"); // find all Ids
      assertThat(sql.get(1)).contains("update coone_many set deleted=true where id  = any(?");
      assertThat(sql.get(2)).contains(" -- bind(Array[1000]="); // update first 1000
      assertThat(sql.get(3)).contains(" -- bind(Array[1000]="); // update first 1000
      assertThat(sql.get(4)).contains(" -- bind(Array[500]="); // update last 500
      assertThat(sql.get(6)).contains("insert into coone_many (coone_id, name, deleted) values (?,?,?)");
      assertThat(sql.get(7)).contains(" -- bind(");
    }

    if (isSqlServer()) {
      assertThat(sql).hasSize(11);
      assertThat(sql.get(0)).contains("select t0.id from coone_many t0 where coone_id=? and t0.deleted = 0 and t0.deleted = 0; --bind"); // find all Ids
      assertThat(sql.get(1)).contains("update coone_many set deleted=1 where id  in (?,?,?");
      assertThat(sql.get(2)).contains(" -- bind(Array[1000]="); // update first 2000
      assertThat(sql.get(3)).contains(" -- bind(Array[1000]="); // update first 2000
      assertThat(sql.get(4)).contains("update coone_many set deleted=1 where id  in (?,?,?");
      assertThat(sql.get(5)).contains(" -- bind(Array[500]="); // update next 500
      assertThat(sql.get(6)).contains(" -- executeBatch() size:2");
      assertThat(sql.get(7)).contains(" -- executeBatch() size:1");
      assertThat(sql.get(8)).contains("insert into coone_many (id, coone_id, name, deleted) values (?,?,?,?)");
      assertThat(sql.get(9)).contains(" -- bind(");
      assertThat(sql.get(10)).contains(" -- executeBatch()"); // update next 500
    }

    COOne fetchedUser2 = DB.find(COOne.class, parentId);
    requireNonNull(fetchedUser2);
    assertThat(fetchedUser2.getChildren())
      .hasSize(2501)
      .extracting(COOneMany::getName)
      .doesNotContain("c0")// filtered
      .contains("c2500")
      .contains("cTest"); // added
  }

  private static List<String> doUpdate(long parentId, Predicate<String> filter) {
    COOne fetchedParent = DB.find(COOne.class, parentId);
    assert fetchedParent != null;

    List<COOneMany> filtered = fetchedParent.getChildren().stream().filter(r -> filter.test(r.getName())).collect(Collectors.toList());

    List<COOneMany> updatedRoles = new ArrayList<>();
    updatedRoles.addAll(filtered);
    updatedRoles.add(new COOneMany("cTest"));
    fetchedParent.setChildren(updatedRoles);

    LoggedSql.start();
    DB.save(fetchedParent);
    return LoggedSql.stop();
  }

  private static long setup(int count) {
    long parentId;
    // setup
    List<COOneMany> children = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      children.add(new COOneMany("c" + i));

    }

    COOne parent = new COOne("p0");
    parent.setChildren(children);

    DB.save(parent);
    parentId = parent.getId();
    return parentId;
  }
}
