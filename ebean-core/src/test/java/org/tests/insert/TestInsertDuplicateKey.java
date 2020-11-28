package org.tests.insert;

import io.ebean.BaseTestCase;
import io.ebean.DuplicateKeyException;
import io.ebean.Ebean;
import io.ebean.annotation.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.draftable.Document;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInsertDuplicateKey extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestInsertDuplicateKey.class);

  @Before
  public void clearDb() {
    server().find(Document.class).asDraft().where().contains("title", "UniqueKey").delete();
  }
  
  @Test(expected = DuplicateKeyException.class)
  public void insert_duplicateKey() {

    Document doc1 = new Document();
    doc1.setTitle("ThisIsAUniqueKey");
    doc1.setBody("one");

    doc1.save();

    Document doc2 = new Document();
    doc2.setTitle("ThisIsAUniqueKey");
    doc2.setBody("clashes with doc1");

    doc2.save();
  }

  @Transactional(batchSize = 100)
  @Test(expected = DuplicateKeyException.class)
  public void insertBatch_duplicateKey() {

    Document doc1 = new Document();
    doc1.setTitle("ThisIsASecondUniqueKey");
    doc1.setBody("one");

    doc1.save();

    Document doc2 = new Document();
    doc2.setTitle("ThisIsASecondUniqueKey");
    doc2.setBody("clash when batch flushed");

    doc2.save();
  }

  @Test
  public void insertBatch_duplicateKey_catchAndContinue() {

    insertTheBatch_duplicateKey_catchAndContinue();

    List<Document> found = Ebean.getDefaultServer()
      .find(Document.class)
      .asDraft()
      .where().startsWith("body", "insertTheBatch_duplicateKey_catchAndContinue")
      .findList();

    assertThat(found).hasSize(1);
    assertThat(found.get(0).getTitle()).isEqualTo("ThisIsAThirdUniqueKey");
    assertThat(found.get(0).getBody()).isEqualTo("insertTheBatch_duplicateKey_catchAndContinue-1");
  }

  @Transactional//(batchSize = 100)
  private void insertTheBatch_duplicateKey_catchAndContinue() {

    Document doc1 = new Document();
    doc1.setTitle("ThisIsAThirdUniqueKey");
    doc1.setBody("insertTheBatch_duplicateKey_catchAndContinue-1");
    doc1.save();

    try {
      Document doc2 = new Document();
      doc2.setTitle("ThisIsAThirdUniqueKey");
      doc2.setBody("insertTheBatch_duplicateKey_catchAndContinue-2");
      doc2.save();

      // flush at this point, fails
      Ebean.getDefaultServer().currentTransaction().flushBatch();
    } catch (DuplicateKeyException e) {
      log.info("duplicate failed but just continue" + e.getMessage());
      try {
        // typically we would use transaction.commitAndContinue()
        // ... this is a rollback and continue type scenario
        // ... more sensible to use a second transaction that do this
        Ebean.getDefaultServer().currentTransaction().getConnection().rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    }

    Document doc0 = new Document();
    doc0.setTitle("ThisIsAThirdUniqueKey");
    doc0.setBody("insertTheBatch_duplicateKey_catchAndContinue-1");
    doc0.save();
  }
}
