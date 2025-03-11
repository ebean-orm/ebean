package org.example.resource;

import io.ebean.DB;
import org.example.records.CourseRecordEntity;
import org.example.records.query.QCourseRecordEntity;
import org.example.resource.query.QResource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceEntityTest {

  @Test
  void testSimpleResource() {
    Locale en = Locale.GERMAN;
    Locale de = Locale.ENGLISH;

    AttributeDescriptor heightDescriptor = new AttributeDescriptor();
    heightDescriptor.setName(new Label());
    heightDescriptor.getName().addLabelText(en,"Height");
    heightDescriptor.getName().addLabelText(de,"Höhe");
    heightDescriptor.setDescription(new Label());
    heightDescriptor.getDescription().addLabelText(en, "Height property");
    heightDescriptor.getDescription().addLabelText(de, "Höhe Eigenschaft");

    DB.save(heightDescriptor);

    AttributeDescriptor widthDescriptor = new AttributeDescriptor();
    widthDescriptor.setName(new Label());
    widthDescriptor.getName().addLabelText(en,"Width");
    widthDescriptor.getName().addLabelText(de,"Breite");
    widthDescriptor.setDescription(new Label());
    widthDescriptor.getDescription().addLabelText(en, "Width property");
    widthDescriptor.getDescription().addLabelText(de, "Breite Eigenschaft");

    DB.save(widthDescriptor);

    Resource resource = new Resource();
    resource.setResourceId("R1");
    resource.setName(new Label());
    resource.getName().addLabelText(en, "R1_en");
    resource.getName().addLabelText(de, "R1_de");

    resource.setAttributeValueOwner(new AttributeValueOwner());
    resource.getAttributeValueOwner().addAttributeValue(new AttributeValue(1, heightDescriptor));
    resource.getAttributeValueOwner().addAttributeValue(new AttributeValue(2, widthDescriptor));

    DB.save(resource);

    QResource qresource = new QResource().resourceId.eq(resource.getResourceId());

    List<Resource> resources = qresource.findList();

    assertThat(resources).isNotEmpty();
  }


}
