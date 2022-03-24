package org.tests.model.m2m;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests M2M with complex where queries.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class TestM2MWithWhere extends BaseTestCase {

  @Test
  public void testQuery() throws Exception {
    createTestData();
    MnyNode node = DB.find(MnyNode.class, 1);


    List<MnyNode> result = DB.find(MnyNode.class).where().eq("allRelations", node).findList();
    assertThat(result).extracting(MnyNode::getId).containsExactlyInAnyOrder(1, 2, 3, 4, 5);

    result = DB.find(MnyNode.class).where().eq("allReverseRelations", node).findList();
    assertThat(result).extracting(MnyNode::getId).containsExactlyInAnyOrder(1, 2, 3, 4, 5);

    result = DB.find(MnyNode.class).where().eq("bit1Relations", node).findList();
    assertThat(result).isEmpty(); // -> to = 1 column: 2 0 2 0 2

    result = DB.find(MnyNode.class).where().eq("bit2Relations", node).findList();
    assertThat(result).extracting(MnyNode::getId).containsExactlyInAnyOrder(1, 3, 5);

    result = DB.find(MnyNode.class).where().eq("bit1ReverseRelations", node).findList();
    // -> from = 1 column: 2 1 3 1 3
    assertThat(result).extracting(MnyNode::getId).containsExactlyInAnyOrder(2, 3, 4, 5);

    result = DB.find(MnyNode.class).where().eq("bit2ReverseRelations", node).findList();
    assertThat(result).hasSize(3).extracting(MnyNode::getId).containsExactlyInAnyOrder(1, 3, 5);

    result = DB.find(MnyNode.class).where().eq("bit2ReverseRelations", node).findList();
    assertThat(result).hasSize(3).extracting(MnyNode::getId).containsExactlyInAnyOrder(1, 3, 5);
  }

  @Test
  public void testGetter() throws Exception {
    createTestData();
    MnyNode node = DB.find(MnyNode.class, 3);

    assertThat(node.getAllRelations()).extracting(MnyNode::getId).containsExactlyInAnyOrder(1, 2, 3, 4, 5);

    assertThat(node.getAllReverseRelations()).extracting(MnyNode::getId).containsExactlyInAnyOrder(1, 2, 3, 4, 5);

    assertThat(node.getBit1Relations()).extracting(MnyNode::getId).containsExactlyInAnyOrder(4, 5);

    assertThat(node.getBit1ReverseRelations()).extracting(MnyNode::getId).containsExactlyInAnyOrder(1, 2);

    assertThat(node.getBit2Relations()).extracting(MnyNode::getId).containsExactlyInAnyOrder(1, 3, 5);

    LoggedSql.start();
    assertThat(node.getBit2ReverseRelations()).extracting(MnyNode::getId).containsExactlyInAnyOrder(1, 3, 5);
    List<String> sqls = LoggedSql.stop();
    assertThat(sqls).hasSize(1); // lazy load

    // prefetch everything
    LoggedSql.start();
    node = DB.find(MnyNode.class)
        .fetch("bit1Relations","*")
        .fetch("bit1ReverseRelations","*")
        .where().idEq(3).findOne();
    sqls = LoggedSql.stop();
    assertThat(sqls).hasSize(2);

    // no lazyLoad expected
    LoggedSql.start();
    assertThat(node.getBit1Relations()).extracting(MnyNode::getId).containsExactlyInAnyOrder(4, 5);
    assertThat(node.getBit1ReverseRelations()).extracting(MnyNode::getId).containsExactlyInAnyOrder(1, 2);
    sqls = LoggedSql.stop();
    assertThat(sqls).hasSize(0);

  }

  // to =     | 1 2 3 4 5
  // ---------+---------------
  // from = 1 | 2 1 3 1 3
  // from = 2 | 0 2 1 3 1
  // from = 3 | 2 0 2 1 3
  // from = 4 | 0 2 0 2 1
  // from = 5 | 2 0 2 0 2
  private void createTestData() {
    DB.find(MnyEdge.class).delete();
    DB.find(MnyNode.class).delete();
    for (int i = 1; i <= 5; i++) {
      MnyNode node = new MnyNode();
      node.setId(i);
      node.setName("Node #" + i);
      DB.save(node);
    }
    StringBuilder sb = new StringBuilder();
    for (int from = 1; from <= 5; from++) {
      sb.append("from = ").append(from).append(" |");
      for (int to = 1; to <= 5; to++) {
        MnyEdge edge = new MnyEdge();
        edge.setFrom(DB.reference(MnyNode.class, from));
        edge.setTo(DB.reference(MnyNode.class, to));
        int flags = 0;
        if (from < to) {
          flags |= 1;
        }
        if ((from + to) % 2 == 0) {
          flags |= 2;
        }
        edge.setFlags(flags);
        DB.save(edge);
        sb.append("  ").append(flags);
      }
      sb.append('\n');
    }
    // System.out.println(sb); dump the table
  }

  @AfterEach
  void deleteTestData() {
    DB.find(MnyEdge.class).delete();
    DB.find(MnyNode.class).delete();
  }

  @Test
  public void testWithDbTableName() {
    LoggedSql.start();
    DB.find(MnyNode.class).where().isNotNull("withDbTableName.name").findList();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("'mny_node' = u1.name");

    LoggedSql.start();
    DB.find(MnyNode.class).where().isNotEmpty("withDbTableName").findList();
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("'mny_node' = x2.name");
  }

  @Test
  public void testLazyLoad() throws Exception {
    MnyNode el = new MnyNode("testLazyLoad");
    DB.save(el);
    LoggedSql.start();
    el = DB.find(MnyNode.class).select("name").where().eq("name", "testLazyLoad").findOne();
    el.getWithDbTableName().size(); // trigger Lazy load
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.id, t0.name from mny_node");
    assertThat(sql.get(1)).contains("where 'mny_node' = t0.name");
    DB.delete(el);
  }

}
