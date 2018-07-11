package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.BBookmark;
import org.tests.model.basic.BBookmarkUser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestInsertManyAndRef extends BaseTestCase {

  @Test
  public void testMe() {

    final BBookmarkUser u = new BBookmarkUser("Mr Test");
    u.setEmailAddress("test@test.com");
    u.setPassword("password");

    final List<BBookmark> bookmarks = new ArrayList<>();
    final BBookmark b1 = new BBookmark();
    b1.setBookmarkReference("Acts 2:7-20");
    b1.setUser(u);

    final BBookmark b2 = new BBookmark();
    b2.setBookmarkReference("Acts 7:1-20");
    b2.setUser(u);

    bookmarks.add(b1);
    bookmarks.add(b2);

    Ebean.saveAll(bookmarks);
  }
}
