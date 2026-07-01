package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.CacheMode;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Article;
import org.tests.model.basic.Section;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestQueryFindReadOnly extends BaseTestCase {

  @Test
  public void test() {

    Section s0 = new Section("some content");
    Article a0 = new Article("art1", "auth1");
    a0.addSection(s0);

    DB.save(a0);

    Article ar1 = DB.find(Article.class)
      .setUnmodifiable(true)
      .fetch("sections")
      .setId(a0.getId()).findOne();

    assertNotNull(ar1);
    assertTrue(DB.beanState(ar1).isUnmodifiable());

    List<Section> ar1sections = ar1.getSections();
    assertEquals(1, ar1sections.size());

    Section s2 = ar1sections.get(0);
    assertTrue(DB.beanState(s2).isUnmodifiable());

    DB.find(Article.class).setBeanCacheMode(CacheMode.PUT).findList();

    Article ar0 = DB.find(Article.class, a0.getId());

    assertNotNull(ar0);
    assertFalse(DB.beanState(ar0).isUnmodifiable());

    List<Section> ar0sections = ar0.getSections();
    Section s1 = ar0sections.get(0);
    assertFalse(DB.beanState(s1).isUnmodifiable());

  }

}
