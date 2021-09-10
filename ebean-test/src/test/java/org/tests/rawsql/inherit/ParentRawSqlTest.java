package org.tests.rawsql.inherit;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.inherit.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParentRawSqlTest extends BaseTestCase {

  @BeforeEach
  public void clearDb() {
    DB.deleteAll(DB.find(Data.class).findList());
    DB.deleteAll(DB.find(Parent.class).findList());
    //@rob: this does not work as it does not clear the ManyToMany relations.
//    DB.find(Data.class).delete();
//    DB.find(Parent.class).delete();
  }

  @Test
  public void RawSqlParentLoad() {

    List<Data> exampleData = new ArrayList<>();
    exampleData.add(new Data(0));
    exampleData.add(new Data(1));
    exampleData.add(new Data(2));

    ChildA a = new ChildA(0, "PA");
    a.setData(exampleData);
    DB.save(a);

    ChildB b = new ChildB(1, "PB");
    b.setData(exampleData);
    DB.save(b);

    ChildA c = new ChildA(2, "PC");
    c.setData(exampleData);
    DB.save(c);

    RawSql rawSql = RawSqlBuilder
      .parse("select type, id, val from rawinherit_parent where val > 1")
      .create();

    List<Parent> partial = DB.find(Parent.class)
      .setRawSql(rawSql)
      .findList();

    assertNotNull(partial.get(0).getData());
    assertEquals(partial.get(0).getData().get(0).getVal().intValue(), 0);
  }

  @Test
  public void RawSqlParentFetch() {

    List<Data> exampleData = new ArrayList<>();
    exampleData.add(new Data(0));
    exampleData.add(new Data(1));
    exampleData.add(new Data(2));

    ChildA a = new ChildA(0, "PA");
    a.setData(exampleData);
    DB.save(a);

    ChildB b = new ChildB(1, "PB");
    b.setData(exampleData);
    DB.save(b);

    ChildA c = new ChildA(2, "PC");
    c.setData(exampleData);
    DB.save(c);

    EUncle e1 = new EUncle();
    e1.setParent(b);
    e1.setName("fester");
    DB.save(e1);

    joinToInheritanceHierarchy_withAliasMapping();
    joinToInheritanceHierarchy_bug416();
    joinToInheritanceHierarchy_with_queryJoin();
    joinToInheritanceHierarchy_withIgnore();

    useColumnMappingIgnore();
    useColumnMappingWithDiscriminator();
    useExtraColumnMappingIgnore();
  }


  private void joinToInheritanceHierarchy_bug416() {

    // For bug 416 we need the parent.more to trigger it

    RawSql rawSql = RawSqlBuilder
      .unparsed("select u.id, u.name, p.type as p_type, p.id as p_id, p.more as p_more from rawinherit_uncle u join rawinherit_parent p on p.id = u.parent_id")
      .columnMapping("id", "id")
      .columnMapping("name", "name")
      .columnMapping("p_type", "parent.type")
      .columnMapping("p_id", "parent.id")
      .columnMapping("p_more", "parent.more")
      .create();

    List<EUncle> uncles = DB.find(EUncle.class).setRawSql(rawSql).findList();

    assertNotNull(uncles.get(0));
    Parent parent = uncles.get(0).getParent();
    assertTrue(parent instanceof ChildB);
  }

  private void joinToInheritanceHierarchy_with_queryJoin() {

    RawSql rawSql = RawSqlBuilder
      .unparsed("select u.id, u.name, p.type as p_type, p.id as p_id from rawinherit_uncle u join rawinherit_parent p on p.id = u.parent_id")
      .columnMapping("id", "id")
      .columnMapping("name", "name")
      .columnMapping("p_type", "parent.type")
      .columnMapping("p_id", "parent.id")
      .create();

    List<EUncle> uncles = DB.find(EUncle.class).setRawSql(rawSql)
      .fetchQuery("parent")
      .findList();

    assertNotNull(uncles.get(0));
    Parent parent = uncles.get(0).getParent();
    assertTrue(parent instanceof ChildB);
  }

  private void joinToInheritanceHierarchy_withIgnore() {

    RawSql rawSql = RawSqlBuilder
      .unparsed("select u.id, u.name, p.type as ptype, p.id as pid from rawinherit_uncle u join rawinherit_parent p on p.id = u.parent_id")
      .columnMapping("id", "id")
      .columnMapping("name", "name")
      .columnMappingIgnore("ptype")
      .columnMapping("pid", "parent.id")
      .create();

    List<EUncle> uncles = DB.find(EUncle.class).setRawSql(rawSql)
      .fetchQuery("parent")
      .findList();

    assertNotNull(uncles.get(0));
    Parent parent = uncles.get(0).getParent();
    assertTrue(parent instanceof ChildB);
  }

  private void joinToInheritanceHierarchy_withAliasMapping() {

    RawSql rawSql = RawSqlBuilder
      .parse("select u.id, u.name, p.type, p.id from rawinherit_uncle u join rawinherit_parent p on p.id = u.parent_id")
      .tableAliasMapping("p", "parent")
      .create();

    List<EUncle> uncles = DB.find(EUncle.class).setRawSql(rawSql)
      .findList();

    assertNotNull(uncles.get(0));
    Parent parent = uncles.get(0).getParent();
    assertTrue(parent instanceof ChildB);
  }

  /**
   * Map the discriminator column - columnMapping("type", "parent.type").
   */
  private void useColumnMappingWithDiscriminator() {

    RawSql rawSql = RawSqlBuilder
      .unparsed("select type, id from rawinherit_parent where val > 1")
      .columnMapping("type", "parent.type") // can map the discriminator column 'type'
      .columnMapping("id", "parent.id")
      .create();

    List<ParentAggregate> aggregates = DB.find(ParentAggregate.class).setRawSql(rawSql)
      .fetchQuery("parent")
      .findList();

    List<Parent> partial = new ArrayList<>();
    for (ParentAggregate aggregate : aggregates) {
      partial.add(aggregate.parent);
    }

    assertNotNull(partial.get(0).getData());
    assertEquals(partial.get(0).getData().get(0).getVal().intValue(), 0);
  }

  /**
   * Use columnMappingIgnore() to ignore the discriminator column.
   */
  private void useColumnMappingIgnore() {

    RawSql rawSql = RawSqlBuilder
      .unparsed("select type, id from rawinherit_parent where val > 1")
      .columnMappingIgnore("type") // can ignore the discriminator 'type'
      .columnMapping("id", "parent.id")
      .create();

    List<ParentAggregate> aggregates = DB.find(ParentAggregate.class).setRawSql(rawSql)
      .fetchQuery("parent")
      .findList();

    List<Parent> partial = new ArrayList<>();
    for (ParentAggregate aggregate : aggregates) {
      partial.add(aggregate.parent);
    }

    assertNotNull(partial.get(0).getData());
    assertEquals(partial.get(0).getData().get(0).getVal().intValue(), 0);
  }

  /**
   * Extra columnMappingIgnore() before and after.
   */
  private void useExtraColumnMappingIgnore() {

    RawSql rawSql = RawSqlBuilder
      .unparsed("select 'a', type, id, 'b' from rawinherit_parent where val > 1")
      .columnMappingIgnore("a") // extra ignore before
      .columnMappingIgnore("type")
      .columnMapping("id", "parent.id")
      .columnMappingIgnore("b") // extra ignore after
      .create();

    List<ParentAggregate> aggregates = DB.find(ParentAggregate.class).setRawSql(rawSql)
      .fetchQuery("parent")
      .findList();

    List<Parent> partial = new ArrayList<>();
    for (ParentAggregate aggregate : aggregates) {
      partial.add(aggregate.parent);
    }
    assertNotNull(partial.get(0).getData());
    assertEquals(partial.get(0).getData().get(0).getVal().intValue(), 0);
  }

}
