package org.tests.insert;

import io.ebean.BaseTestCase;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.draftable.Document;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInsertCheckUnique extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestInsertCheckUnique.class);

  @Before
  public void clearDb() {
    server().find(Document.class).asDraft().where().contains("title", "UniqueKey").delete();
  }
  
  @Test
  public void insert_duplicateKey() {
    server().beginTransaction();
    try {
      Document doc1 = new Document();
      doc1.setTitle("ThisIsAUniqueKey");
      doc1.setBody("one");

      assertThat(server().checkUniqueness(doc1)).isEmpty();
      doc1.save();

      Document doc2 = new Document();
      doc2.setTitle("ThisIsAUniqueKey");
      doc2.setBody("clashes with doc1");

      assertThat(server().checkUniqueness(doc2)).isEmpty();
      ;

      server().publish(doc1.getClass(), doc1.getId());

      assertThat(server().checkUniqueness(doc2).toString()).contains("title");
    } finally {
      server().endTransaction();
    }
  }

}
