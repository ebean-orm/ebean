package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DuplicateKeyException;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.draftable.Document;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTransactionTryResources extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestTransactionTryResources.class);

  @Test
  public void tryWithResources_simple() {

    Document doc = new Document();

    try (Transaction transaction = Ebean.beginTransaction()) {

      doc.setTitle("tryWithResources");
      doc.setBody("stuff");
      doc.save();
      transaction.commit();
    }

    Document document = Document.find.asDraft(doc.getId());
    assertThat(document).isNotNull();
    assertThat(document.isDraft()).isTrue();

    // cleanup
    Ebean.delete(document);
  }

  @Test
  @IgnorePlatform(Platform.ORACLE) // does not support uncommited reads
  public void tryWithResources_catch() {
    Document doc = null;
    try (Transaction transaction = Ebean.beginTransaction()) {

      doc = new Document();
      doc.setTitle("tryWithResources_catch");
      doc.setBody("tryWithResources_catch_1");
      doc.save();

      Document doc2 = new Document();
      doc2.setTitle("tryWithResources_catch");
      doc2.setBody("tryWithResources_catch_2");
      doc2.save();

      transaction.commit();

    } catch (DuplicateKeyException e) {

      log.info("catch duplicate ... " + e);

      Document doc3 = new Document();
      doc3.setTitle("tryWithResources_catch");
      doc3.setBody("tryWithResources_catch_3");
      doc3.save();

      List<Document> docs = Document.find.query()
        .where().startsWith("body", "tryWithResources_catch")
        .asDraft()
        .findList();

      assertThat(docs).hasSize(1);
      assertThat(doc).isNotNull();
      // Cleanup
      Ebean.delete(Document.class, doc.getId());
      Ebean.delete(Document.class, doc3.getId());
    }
  }
}
