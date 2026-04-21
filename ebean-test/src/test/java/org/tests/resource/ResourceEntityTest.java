package org.tests.resource;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceEntityTest {

  static AttributeDescriptor heightDescriptor;
  static AttributeDescriptor widthDescriptor;
  static Resource resource;

  @BeforeAll
  static void setup() {
    Locale en = Locale.GERMAN;
    Locale de = Locale.ENGLISH;

    heightDescriptor = new AttributeDescriptor();
    heightDescriptor.setName(new Label());
    heightDescriptor.getName().addLabelText(en, "Height");
    heightDescriptor.getName().addLabelText(de, "Höhe");
    heightDescriptor.setDescription(new Label());
    heightDescriptor.getDescription().addLabelText(en, "Height property");
    heightDescriptor.getDescription().addLabelText(de, "Höhe Eigenschaft");

    DB.save(heightDescriptor);

    widthDescriptor = new AttributeDescriptor();
    widthDescriptor.setName(new Label());
    widthDescriptor.getName().addLabelText(en, "Width");
    widthDescriptor.getName().addLabelText(de, "Breite");
    widthDescriptor.setDescription(new Label());
    widthDescriptor.getDescription().addLabelText(en, "Width property");
    widthDescriptor.getDescription().addLabelText(de, "Breite Eigenschaft");

    DB.save(widthDescriptor);

    resource = new Resource();
    resource.setResourceId("R1");
    resource.setName(new Label());
    resource.getName().addLabelText(en, "R1_en");
    resource.getName().addLabelText(de, "R1_de");

    resource.setAttributeValueOwner(new AttributeValueOwner());
    resource.getAttributeValueOwner().addAttributeValue(new AttributeValue(1, heightDescriptor));
    resource.getAttributeValueOwner().addAttributeValue(new AttributeValue(2, widthDescriptor));

    DB.save(resource);
  }

  @Test
  void testSimpleResource() {
    var resources = DB.find(Resource.class)
      .where().eq("resourceId", resource.getResourceId())
      .findList();

    assertThat(resources).isNotEmpty();
  }

  @Test
  void find_then_invokeLazyLoading_expect_singleLazyLoadingQueryForSameType() {
    AttributeDescriptor one = DB.find(AttributeDescriptor.class).setId(heightDescriptor.id())
      .findOne();

    assertThat(one)
      .describedAs("setup data exists")
      .isNotNull();

    LoggedSql.start();
    assertThat(one.getName().version())
      .describedAs("lazy loaded name label")
      .isGreaterThan(0);

    assertThat(one.getDescription().version())
      .describedAs("lazy loaded description label")
      .isGreaterThan(0);

    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
    assertThat(sql.get(0))
      .describedAs("lazy loading query invoked by getName/getDescription")
      .contains("select t0.id, t0.version from label t0 where t0.id in ");
  }

}
