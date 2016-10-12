package com.avaje.tests.query;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Article;
import com.avaje.tests.model.basic.Section;
import com.avaje.tests.model.basic.Slot;

public class TestNamedQueryWithNamedParam extends BaseTestCase {

  
  @Test
  public void testInvalidEQL() {
	  Section s0 = new Section("some content");
	  String author1 = "auth1";
	  Article a0 = new Article("art1", author1);
	  a0.addSection(s0);

	  Ebean.save(a0);

	  Section s1 = new Section("some content");
	  String author2 = "auth2";
	  Article a1 = new Article("art2", author2);
	  a1.addSection(s1);

	  Ebean.save(a1);

	  List<Article> list =   Ebean.find(Article.class).where().eq("author", author1).findList();
	  
	  Assert.assertTrue(list.size() ==1);    //<<<<<<<<<<<<< Works fine
	  
	  Query<Article> query = Ebean.createQuery(Article.class, "find Article where name = :p0"); // Should throw an exception
	  query.setParameter("p0", author1);
	  list =  query.findList();
	  
	  Assert.assertTrue("Shouldn't get this far as the query is syntactically incorrect", list.size() ==1);     // <<<<<<<<<<<<< fails 
	 
  }

  @Test
  public void testNamedQuery() {
		final Query<Slot> query = 
				Ebean.createNamedQuery(Slot.class, "findOverlappingSlots");
		
//		query.setParameter("thisOID", 1);
//		query.setParameter("swimlaneOid", 1);
		query.setParameter("thisPlannedBegin", new DateTime());
//		query.setParameter("thisPlannedEnd", new DateTime());
//		
		query.findList();
  }

}
