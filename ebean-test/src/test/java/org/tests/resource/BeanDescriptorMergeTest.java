package org.tests.resource;

import io.ebean.DB;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class BeanDescriptorMergeTest {

  @Test
  void merge_copiesUnloadedScalar_andMarksLoaded() {

    long id = createLabel("merge-copy-loaded");
    BeanDescriptor<Label> descriptor = descriptor();

    Label from = DB.find(Label.class).setId(id).select("version").findOne();
    Label existing = DB.reference(Label.class, id);

    EntityBeanIntercept fromEbi = ((EntityBean) from)._ebean_getIntercept();
    EntityBeanIntercept toEbi = ((EntityBean) existing)._ebean_getIntercept();
    int versionIndex = fromEbi.findProperty("version");

    assertThat(toEbi.isLoadedProperty(versionIndex)).isFalse();

    descriptor.merge((EntityBean) from, (EntityBean) existing);

    assertThat(existing.version()).isEqualTo(from.version());
    assertThat(toEbi.isLoadedProperty(versionIndex)).isTrue();
  }

  @Test
  void merge_doesNotOverwriteAlreadyLoadedScalar() {

    long id = createLabel("merge-no-overwrite");
    BeanDescriptor<Label> descriptor = descriptor();

    Label from = DB.find(Label.class).setId(id).select("version").findOne();
    Label existing = DB.find(Label.class).setId(id).findOne();
    long changedVersion = existing.version() + 100;
    existing.version(changedVersion);

    descriptor.merge((EntityBean) from, (EntityBean) existing);

    assertThat(existing.version()).isEqualTo(changedVersion);
  }

  @Test
  void merge_fallbackWhenPropertiesIndexEntryMissing() throws Exception {

    long id = createLabel("merge-fallback");
    BeanDescriptor<Label> descriptor = descriptor();

    Label from = DB.find(Label.class).setId(id).select("version").findOne();
    Label existing = DB.reference(Label.class, id);

    EntityBeanIntercept fromEbi = ((EntityBean) from)._ebean_getIntercept();
    EntityBeanIntercept toEbi = ((EntityBean) existing)._ebean_getIntercept();
    int versionIndex = fromEbi.findProperty("version");

    Field field = BeanDescriptor.class.getDeclaredField("propertiesIndex");
    field.setAccessible(true);
    BeanProperty[] propertiesIndex = (BeanProperty[]) field.get(descriptor);
    BeanProperty original = propertiesIndex[versionIndex];
    propertiesIndex[versionIndex] = null;
    try {
      descriptor.merge((EntityBean) from, (EntityBean) existing);
    } finally {
      propertiesIndex[versionIndex] = original;
    }

    assertThat(existing.version()).isEqualTo(from.version());
    assertThat(toEbi.isLoadedProperty(versionIndex)).isTrue();
  }

  private BeanDescriptor<Label> descriptor() {
    return ((SpiEbeanServer) DB.getDefault()).descriptor(Label.class);
  }

  private long createLabel(String textPrefix) {
    Label label = new Label();
    label.addLabelText(Locale.ENGLISH, textPrefix + "-en");
    label.addLabelText(Locale.GERMAN, textPrefix + "-de");
    DB.save(label);
    return label.id();
  }
}
