package org.tests.draftable;

import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.Test;
import org.tests.model.draftable.Document;
import org.tests.model.draftable.DocumentMedia;
import org.tests.model.draftable.Organisation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OrganisationTest {

  @Test
  public void testSave() {

    Organisation org = new Organisation("OrgOne");
    org.save();

    assertNotNull(org.getId());

    Document doc = new Document();
    doc.setTitle("NewTitle");
    doc.setOrganisation(org);
    doc.setBody("Hello");

    doc.save();

    doc.setBody("Change content");
    doc.save();


    Database server = DB.getDefault();

    Document draftDoc = server.find(Document.class)
      //.asDraft()
      .setId(doc.getId())
      .findOne();

    assertNotNull(draftDoc);

    //server.publish(Document.class, doc.getId(), null);

    doc.setTitle("Mod1");
    doc.save();

    //server.publish(Document.class, doc.getId(), null);
  }


  @Test
  public void testSaveWithCascade() {

    Organisation org = new Organisation("Org2");
    org.save();

    Document doc = new Document();
    doc.setTitle("Title1");
    doc.setOrganisation(org);
    doc.setBody("Body1");

    doc.getMedia().add(createMedia("media1"));
    doc.getMedia().add(createMedia("media2"));
    doc.save();

    Database server = DB.getDefault();


    //server.publish(Document.class, doc.getId(), null);

    Document fetchDoc = DB.find(Document.class).setId(doc.getId()).findOne(); //.asDraft()
    List<DocumentMedia> media = fetchDoc.getMedia();

    assertThat(media.size()).isEqualTo(2);

//    // delete one of the 'child' @DraftElement rows ...
//    SqlUpdate sqlUpdate = DB.sqlUpdate("delete from document_media_draft where id = ?");
//    sqlUpdate.setParameter(1, doc.getMedia().get(0).getId());
//    sqlUpdate.execute();

    doc.getMedia().get(1).setDescription("mod");
    doc.getMedia().add(createMedia("media3"));
    doc.getMedia().remove(0);
    doc.setBody("Body2");
    doc.save();

    // publish will perform an insert, update and delete on child DocumentMedia
    // during the publish below with media1 being deleted
    Document liveBean = doc;//server.publish(Document.class, doc.getId(), null);
    assertThat(liveBean.getBody()).isEqualTo("Body2");
    assertThat(liveBean.getMedia().size()).isEqualTo(2);
    assertThat(liveBean.getMedia()).extracting("name").contains("media2", "media3");

  }

  private DocumentMedia createMedia(String name) {
    DocumentMedia media = new DocumentMedia();
    media.setName(name);
    return media;
  }
}
