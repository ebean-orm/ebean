package org.tests.query.other;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.converstation.Conversation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQueryConversationRowCount extends BaseTestCase {

  @Test
  public void test() {

    //"find conversation where club = :clubId and ( ( isPublic = :false and participants.user.id = :userId ) or isPublic = :true ) order by createdAt desc ";
    //"find conversation where groupId = :groupId and ( ( open = :false and participants.user.id = :userId ) or open = :true ) order by whenCreated desc ";

    Long groupId = 1L;
    Long userId = 1L;

    Query<Conversation> query = DB.find(Conversation.class)
      .where().eq("group.id", groupId)
      .disjunction()
      .conjunction()
      .eq("open", false).eq("participants.user.id", userId)
      .endJunction()
      .eq("open", true)
      .endJunction()
      .orderBy("whenCreated desc").query();

    query.findList();
    String generatedSql = sqlOf(query, 1);

    // select distinct t0.id c0, t0.title c1, t0.open c2, t0.version c3, t0.when_created c4, t0.when_updated c5, t0.group_id c6, t0.when_created
    // from c_conversation t0
    // left join c_participation u1 on u1.conversation_id = t0.id
    // where t0.group_id = ?  and ((t0.open = ?  and u1.user_id = ? )  or t0.open = ? )
    // order by t0.when_created desc;

    if (platformDistinctOn()) {
      assertThat(generatedSql).contains("select distinct on (t0.when_created, t0.id) t0.id, t0.title, t0.isopen");

    } else {
      assertThat(generatedSql).contains("select distinct t0.id, t0.title, t0.isopen");
    }
    assertThat(generatedSql).contains("left join c_participation u1 on u1.conversation_id = t0.id");
    assertThat(generatedSql).contains("where t0.group_id = ? and ((t0.isopen = ? and u1.user_id = ?) or t0.isopen = ?)");


    LoggedSql.start();
    query.findCount();

    // select count(*) from (
    //   select distinct t0.id c0
    //   from c_conversation t0
    //   left join c_participation u1 on u1.conversation_id = t0.id
    //   where t0.group_id = ?  and ((t0.open = ?  and u1.user_id = ? )  or t0.open = ? )
    // ); --bind(1,true,1,true)

    List<String> loggedSql = LoggedSql.stop();
    assertEquals(1, loggedSql.size());

    String countSql = trimSql(loggedSql.get(0), 0);
    assertThat(countSql).contains("select count(*) from ( select distinct t0.id from c_conversation t0 left join c_participation u1 on u1.conversation_id = t0.id where t0.group_id = ? and ((t0.isopen = ? and u1.user_id = ?) or t0.isopen = ?))");
  }

}
