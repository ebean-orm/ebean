package com.avaje.tests.basic;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Article;
import com.avaje.tests.model.basic.Section;
import junit.framework.TestCase;

public class TestDeleteCascadingOneToMany extends TestCase {

  public void testDeleteCascadingOneToMany() {
    Section s0 = new Section("some content");
    Article a0 = new Article("art1", "auth1");
    a0.addSection(s0);

    Ebean.save(a0);

    Ebean.delete(a0);
  }

}
