package org.tests.draftable;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.draftable.Doc;
import org.tests.model.draftable.Link;

import javax.persistence.PersistenceException;
import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DocLinkTest extends BaseTestCase {

  @Test
  public void testLazyLoadOnDraftProperty() {

    Link link1 = new Link("something");
    link1.save();

    DB.getDefault().publish(Link.class, link1.getId());

    Link link = DB.find(Link.class)
      .setId(link1.getId())
      .select("name")
      .findOne();

    assertThat(link).isNotNull();

    LoggedSql.start();

    // no lazy loading is invoked as draft property considered @Transient
    assertThat(link.isDraft()).isFalse();

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).isEmpty();

  }

  @Test
  public void testUpdate_whenNotPublished() {

    Link link1 = new Link("update");
    assertThat(link1.isDraft()).isFalse();

    link1.save();
    assertThat(link1.isDraft()).isTrue();

    // perform stateless update
    Link linkUpdate = new Link();
    linkUpdate.setId(link1.getId());
    linkUpdate.setComment("stateless update");
    linkUpdate.setDraft(true);
    linkUpdate.update();

    // invoke lazy loading on the updated bean
    // automatically set asDraft() on lazy loading query
    linkUpdate.getLocation();

    DB.deletePermanent(linkUpdate);
  }

  @Test
  public void testDelete_whenNotPublished() {

    Link link1 = new Link("Ld1");
    assertThat(link1.isDraft()).isFalse();

    link1.save();
    assertThat(link1.isDraft()).isTrue();

    link1.setComment("some change");
    link1.save();

    DB.delete(link1);
  }

  @Test
  public void testDeletePermanent_whenPublished2() {

    Link link1 = new Link("Ld2");
    link1.save();
    DB.getDefault().publish(Link.class, link1.getId());

    Link link = DB.find(Link.class).setId(link1.getId()).asDraft().findOne();
    DB.deletePermanent(link);
  }

  @Test
  public void testDeleteLivePermanent_throwsException() {

    Link link1 = new Link("Ld2");
    link1.save();

    Link live = DB.getDefault().publish(Link.class, link1.getId());

    try {
      DB.deletePermanent(live);
      fail("never get here");

    } catch (PersistenceException e) {
      // assert nice message when trying to delete live bean
      assertThat(e.getMessage()).contains("Explicit Delete is not allowed on a 'live' bean - only draft beans");
    }
  }

  @Test
  public void testDelete_whenPublished() {

    Link link1 = new Link("Ld2");
    link1.save();
    Database server = DB.getDefault();
    server.publish(Link.class, link1.getId());

    link1 = DB.find(Link.class).setId(link1.getId()).asDraft().findOne();
    assertThat(link1.isDraft()).isTrue();

    // this is a soft delete (no automatic publish here, only updates draft)
    link1.delete();

    Link live = DB.find(Link.class).setId(link1.getId()).findOne();
    assertThat(live).isNotNull();
    assertThat(live.isDraft()).isFalse();
    assertThat(live.isDeleted()).isFalse(); // soft delete state not published yet

    // this is a permanent delete (effectively has automatic publish)
    server.deletePermanent(link1);

    live = DB.find(Link.class).setId(link1.getId()).findOne();
    assertThat(live).isNull();
  }

  @Test
  public void testUpdateLive_throwsException() {

    Link link1 = new Link("forUpdateLive");
    link1.save();

    Link live = DB.getDefault().publish(Link.class, link1.getId());

    live.setComment("foo");
    // Expect a nice
    try {
      live.save();
      fail("Never get here");

    } catch (PersistenceException e) {
      // we want to assert the message is nice and meaningful (and not a optimistic locking exception etc)
      assertThat(e.getMessage()).contains("Save or update is not allowed on a 'live' bean - only draft beans");
    }
  }

  @Test
  public void testDirtyState() {
    Timestamp when = new Timestamp(System.currentTimeMillis());
    String comment = "Really interesting";

    Link link1 = new Link("Ls1");
    link1.setComment(comment);
    link1.setWhenPublish(when);
    link1.save();

    Link draft1 = DB.find(Link.class).setId(link1.getId()).asDraft().findOne();
    assertThat(draft1.isDirty()).isTrue();

    Database server = DB.getDefault();

    Link linkLive = server.publish(Link.class, link1.getId(), null);
    assertThat(linkLive.getComment()).isEqualTo(comment);
    assertThat(linkLive.getWhenPublish()).isEqualToIgnoringMillis(when);

    Link draft1b = DB.find(Link.class).setId(link1.getId()).asDraft().findOne();
    assertThat(draft1b.isDirty()).isFalse();
    assertThat(draft1b.getComment()).isNull();
    assertThat(draft1b.getWhenPublish()).isNull();
  }

  @Test
  public void testSave() {

    Link link1 = new Link("LinkOne");
    link1.save();

    Link link2 = new Link("LinkTwo");
    link2.save();

    Link link3 = new Link("LinkThree");
    link3.save();

    Database server = DB.getDefault();
    server.publish(Link.class, link1.getId(), null);
    server.publish(Link.class, link2.getId(), null);
    server.publish(Link.class, link3.getId(), null);

    Doc doc1 = new Doc("DocOne");
    doc1.getLinks().add(link1);
    doc1.getLinks().add(link2);
    doc1.save();

    Doc draftDoc1 = server.find(Doc.class)
      .setId(doc1.getId())
      .asDraft()
      .findOne();

    assertThat(draftDoc1.getLinks()).hasSize(2);

    Doc liveDoc1 = server.publish(Doc.class, doc1.getId(), null);

    assertThat(liveDoc1.getLinks()).hasSize(2);
    assertThat(liveDoc1.getLinks()).extracting("id").contains(link1.getId(), link2.getId());


    draftDoc1.getLinks().remove(0);
    Link remaining = draftDoc1.getLinks().get(0);
    draftDoc1.getLinks().add(link3);

    draftDoc1.save();

    // publish with insert and delete of Links M2M relationship
    Doc liveDoc2 = server.publish(Doc.class, doc1.getId(), null);
    assertThat(liveDoc2.getLinks()).hasSize(2);
    assertThat(liveDoc2.getLinks()).extracting("id").contains(remaining.getId(), link3.getId());

    // delete the draft and live beans (with associated children)
    draftDoc1.delete();
  }


  @Test
  public void testDraftRestore() {

    Link link1 = new Link("Ldr1");
    link1.setLocation("firstLocation");
    link1.save();

    Database server = DB.getDefault();

    Link live = server.publish(Link.class, link1.getId(), null);
    assertThat(live.isDraft()).isFalse();

    Link draftLink = DB.find(Link.class)
      .setId(link1.getId())
      .asDraft()
      .findOne();

    draftLink.setLocation("secondLocation");
    draftLink.save();

    server.draftRestore(Link.class, link1.getId(), null);

    draftLink = DB.find(Link.class)
      .setId(link1.getId())
      .asDraft()
      .findOne();

    assertThat(draftLink.getLocation()).isEqualTo("firstLocation");
  }

  @Test
  public void testDraftRestoreViaQuery() {

    Link link1 = new Link("Ldr1");
    link1.setLocation("firstLocation");
    link1.setComment("Banana");
    link1.save();

    Database server = DB.getDefault();

    server.publish(Link.class, link1.getId(), null);

    Link draftLink = DB.find(Link.class)
      .setId(link1.getId())
      .asDraft()
      .findOne();

    draftLink.setLocation("secondLocation");
    draftLink.setComment("A good change");
    draftLink.save();

    Query<Link> query = server.find(Link.class).where().eq("id", link1.getId()).query();
    List<Link> links = server.draftRestore(query);

    assertThat(links).hasSize(1);
    assertThat(links.get(0).getLocation()).isEqualTo("firstLocation");
    assertThat(links.get(0).isDirty()).isEqualTo(false);
    assertThat(links.get(0).getComment()).isNull();
  }
}
