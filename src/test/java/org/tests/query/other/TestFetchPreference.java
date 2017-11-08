package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.converstation.Conversation;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class TestFetchPreference extends BaseTestCase {

  @Test
  public void fetchPreference_overrideOrder() {

    Query<Conversation> query = Ebean.find(Conversation.class)
      // FetchPreference overrides so participants is joined and messages query joined
      .fetch("messages")
      .fetch("participants");

    query.findList();
    String sql = sqlOf(query, 1);
    assertThat(sql).contains(" from c_conversation t0 left join c_participation t1 ");
  }


  @Test
  public void fetchPreference_sr() {

    Query<Conversation> query = Ebean.find(Conversation.class)
      // without FetchPreference this ToMany would be our first ToMany join
      // and participants would be query joined
      .fetch("group.users")
      .fetch("messages")
      .fetch("participants");

    query.findList();
    String sql = sqlOf(query, 1);

    // join to group (the ToOne part only) and participants (our preferred ToMany path)
    assertThat(sql).contains(" from c_conversation t0 left join c_group t1 on t1.id = t0.group_id  left join c_participation t2");
  }

  @Test
  public void fetchPreference_inOrder() {

    Query<Conversation> query = Ebean.find(Conversation.class)
      .fetch("participants")
      .fetch("messages");

    query.findList();
    String sql = sqlOf(query, 1);
    assertThat(sql).contains(" from c_conversation t0 left join c_participation t1 ");
  }

  @Test
  public void fetchQuery_onlyOneJoinCandidate() {

    Query<Conversation> query = Ebean.find(Conversation.class)
      .fetch("messages")
      .fetchQuery("participants");

    query.findList();
    String sql = sqlOf(query, 1);

    // participants is explicitly a "query join" so we can join to message
    assertThat(sql).contains(" from c_conversation t0 left join c_message t1 ");
  }
}
