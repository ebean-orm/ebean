package org.tests.insert;

import io.ebean.BaseTestCase;
import io.ebean.plugin.Property;
import org.junit.Before;
import org.junit.Test;
import org.tests.model.draftable.Document;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInsertCheckUnique extends BaseTestCase {

  @Before
  public void clearDb() {
    server().find(Document.class).asDraft().where().contains("title", "UniqueKey").delete();
    server().find(Document.class).asDraft().where().isNull("title").delete();
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

      server().publish(doc1.getClass(), doc1.getId());

      assertThat(server().checkUniqueness(doc2).toString()).contains("title");
    } finally {
      server().endTransaction();
    }
  }

  @Test
  public void insert_duplicateNull() {
    server().beginTransaction();
    try {
      Document doc1 = new Document();
      doc1.setTitle(null);
      doc1.setBody("one");

      assertThat(server().checkUniqueness(doc1)).isEmpty();
      doc1.save();

      Document doc2 = new Document();
      doc2.setTitle(null);
      doc2.setBody("clashes with doc1");

      assertThat(server().checkUniqueness(doc2)).isEmpty();

      server().publish(doc1.getClass(), doc1.getId());

      assertThat(server().checkUniqueness(doc2)).isEmpty();

      doc2.save();
      server().publish(doc2.getClass(), doc2.getId());
    } finally {
      server().endTransaction();
    }
  }

  @Test
  public void example() {

    server().beginTransaction();
    try {
      Document doc1 = new Document();
      doc1.setTitle("One flew over the cuckoo's nest");
      doc1.setBody("one");
      doc1.save();

      server().publish(doc1.getClass(), doc1.getId());

      Document doc2 = new Document();
      doc2.setTitle("One flew over the cuckoo's nest");
      doc2.setBody("clashes with doc1");

      Set<Property> properties = server().checkUniqueness(doc2);
      if (properties.isEmpty()) {
        // it is unique ... carry on
      } else {
        // build a user friendly message
        // to return message back to user

        String uniqueProperties = properties.toString();

        StringBuilder msg = new StringBuilder();

        properties.forEach((it)-> {
          Object propertyValue = it.getVal(doc2);
          String propertyName = it.getName();
          msg.append(" property["+propertyName+"] value["+propertyValue+"]");
        });

        System.out.println("uniqueProperties > "+uniqueProperties);
        System.out.println("      custom msg > " + msg.toString());

      }


      assertThat(server().checkUniqueness(doc2).toString()).contains("title");
    } finally {
      server().endTransaction();
    }
  }
}
