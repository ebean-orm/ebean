package com.avaje.tests.query;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Article;
import com.avaje.tests.model.basic.Section;

public class TestQueryFindReadOnly extends BaseTestCase {

  @Test
  public void test() {

    Section s0 = new Section("some content");
    Article a0 = new Article("art1", "auth1");
    a0.addSection(s0);

    Ebean.save(a0);

    Article ar1 = Ebean.find(Article.class).setReadOnly(true).setId(a0.getId()).findUnique();

    Assert.assertNotNull(ar1);
    Assert.assertTrue("readonly", Ebean.getBeanState(ar1).isReadOnly());

    List<Section> ar1sections = ar1.getSections();
    Assert.assertEquals(1, ar1sections.size());

    Section s2 = ar1sections.get(0);
    Assert.assertTrue("readonly cascading", Ebean.getBeanState(s2).isReadOnly());

    Ebean.find(Article.class).setLoadBeanCache(true).findList();

    Article ar0 = Ebean.find(Article.class, a0.getId());

    Assert.assertNotNull(ar0);
    Assert.assertFalse(Ebean.getBeanState(ar0).isReadOnly());

    List<Section> ar0sections = ar0.getSections();
    Section s1 = ar0sections.get(0);
    Assert.assertFalse(Ebean.getBeanState(s1).isReadOnly());

  }

}
