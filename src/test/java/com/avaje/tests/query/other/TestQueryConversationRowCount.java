package com.avaje.tests.query.other;

import java.util.List;

import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.converstation.Conversation;

public class TestQueryConversationRowCount extends BaseTestCase {

  @Test
  public void test() {
    
    //"find conversation where club = :clubId and ( ( isPublic = :false and participants.user.id = :userId ) or isPublic = :true ) order by createdAt desc ";
    //"find conversation where groupId = :groupId and ( ( open = :false and participants.user.id = :userId ) or open = :true ) order by whenCreated desc ";
    
    Long groupId = 1L;
    Long userId = 1L;
    
    Query<Conversation> query = Ebean.find(Conversation.class)
      .where().eq("group.id", groupId)
      .disjunction()
        .conjunction()
          .eq("open", false).eq("participants.user.id", userId)
        .endJunction()
        .eq("open", true)
      .endJunction()
      .orderBy("whenCreated desc");
    
    query.findList();
    String generatedSql = query.getGeneratedSql();  
    
    // select distinct t0.id c0, t0.title c1, t0.open c2, t0.version c3, t0.when_created c4, t0.when_updated c5, t0.group_id c6, t0.when_created 
    // from c_conversation t0 
    // left outer join c_participation u1 on u1.conversation_id = t0.id  
    // where t0.group_id = ?  and ((t0.open = ?  and u1.user_id = ? )  or t0.open = ? ) 
    // order by t0.when_created desc; 

    Assert.assertTrue(generatedSql.contains("select distinct t0.id c0, t0.title c1, t0.isopen"));
    Assert.assertTrue(generatedSql.contains("left outer join c_participation u1 on u1.conversation_id = t0.id"));
    Assert.assertTrue(generatedSql.contains("where t0.group_id = ?  and ((t0.isopen = ?  and u1.user_id = ? )  or t0.isopen = ? )"));

    
    LoggedSqlCollector.start();
    query.findRowCount();

    // select count(*) from ( 
    //   select distinct t0.id c0 
    //   from c_conversation t0 
    //   left outer join c_participation u1 on u1.conversation_id = t0.id  
    //   where t0.group_id = ?  and ((t0.open = ?  and u1.user_id = ? )  or t0.open = ? ) 
    // ); --bind(1,true,1,true)
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(1, loggedSql.size());
    
    String countSql = loggedSql.get(0);
    
    Assert.assertTrue(countSql.contains("select count(*) from ( select distinct t0.id c0 from c_conversation t0 left outer join c_participation u1 on u1.conversation_id = t0.id  where t0.group_id = ?  and ((t0.isopen = ?  and u1.user_id = ? )  or t0.isopen = ? )"));
  }

}
