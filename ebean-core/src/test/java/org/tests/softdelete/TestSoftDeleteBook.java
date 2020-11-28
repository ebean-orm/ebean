package org.tests.softdelete;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.softdelete.ESoftDelBook;
import org.tests.model.softdelete.ESoftDelUser;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSoftDeleteBook extends BaseTestCase {

  @Test
  public void test() {

    // Create users
    ESoftDelUser user1 = new ESoftDelUser("user1");
    Ebean.save(user1);

    ESoftDelUser user2 = new ESoftDelUser("user2");
    Ebean.save(user2);

    ESoftDelUser user3 = new ESoftDelUser("user3");
    Ebean.save(user3);

    // Create books
    ESoftDelBook book1 = new ESoftDelBook("book1");
    book1.setLendBy(user1);
    book1.setLendBys(Arrays.asList(user2, user3));
    Ebean.save(book1);

    ESoftDelBook book2 = new ESoftDelBook("book2");
    book2.setLendBy(user2);
    book2.setLendBys(Arrays.asList(user1, user3));
    Ebean.save(book2);

    // check if everything is stored correctly in DB
    book1 = Ebean.find(ESoftDelBook.class).where().eq("bookTitle", "book1").findOne();
    book2 = Ebean.find(ESoftDelBook.class).where().eq("bookTitle", "book2").findOne();

    assertThat(book1.getLendBys().size()).isEqualTo(2);
    assertThat(book2.getLendBys().size()).isEqualTo(2);
    assertThat(book1.getLendBy().getUserName()).isEqualTo("user1");
    assertThat(book2.getLendBy().getUserName()).isEqualTo("user2");

    // delete user 1
    Ebean.delete(user1);

    // check if everything is still stored correctly in DB (softdeletes included)
    book1 = Ebean.find(ESoftDelBook.class).where().eq("bookTitle", "book1").setIncludeSoftDeletes().findOne();
    book2 = Ebean.find(ESoftDelBook.class).where().eq("bookTitle", "book2").setIncludeSoftDeletes().findOne();

    assertThat(book1.getLendBys().size()).isEqualTo(2);
    assertThat(book2.getLendBys().size()).isEqualTo(2);
    assertThat(book1.getLendBy().getUserName()).isEqualTo("user1");
    assertThat(book2.getLendBy().getUserName()).isEqualTo("user2");


    // check without softdeletes included
    book1 = Ebean.find(ESoftDelBook.class).where().eq("bookTitle", "book1").findOne();
    book2 = Ebean.find(ESoftDelBook.class).where().eq("bookTitle", "book2").findOne();

    assertThat(book1.getLendBys().size()).isEqualTo(2); // user2 & user3
    assertThat(book2.getLendBys().size()).isEqualTo(1); // user1 (deleted) & user3
    assertThat(book2.getLendBy().getUserName()).isEqualTo("user2");

    // expected behaviour:
    //    book1.getLendBy() == null
    // current behaviour:
    //    book1.getLendBy() is a "dead" object. nearly every operation on the
    //    user object leads into an EntityNotFoundException

    // lendBy is not null (as FK key value set, lendBy is non null reference bean)
    ESoftDelUser lendBy = book1.getLendBy();
    assertThat(lendBy.isDeleted()).isTrue();
  }

  @Test
  public void test_fetch_whenAllManySoftDeleted() {

    // Create users
    ESoftDelUser user1 = new ESoftDelUser("user1");
    Ebean.save(user1);

    ESoftDelUser user2 = new ESoftDelUser("user2");
    Ebean.save(user2);

    // Create books
    ESoftDelBook book1 = new ESoftDelBook("book3");
    book1.setLendBy(user1);
    book1.setLendBys(Arrays.asList(user1, user2));
    Ebean.save(book1);

    Ebean.delete(user1);
    Ebean.delete(user2);

    Query<ESoftDelBook> query = Ebean.find(ESoftDelBook.class)
      .setId(book1.getId())
      .fetch("lendBys");

    ESoftDelBook found = query.findOne();

    assertThat(found).isNotNull();
  }
}
