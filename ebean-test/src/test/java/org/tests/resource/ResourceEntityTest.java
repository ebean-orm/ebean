package org.tests.resource;

import io.ebean.DB;
import io.ebean.ImmutableBeanCache;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

  @Test
  void find_usingImmutableLabelCache_expect_noLazyLoadingQuery() {

    Map<Object, Label> cachedLabels = new LinkedHashMap<>();
    cachedLabels.put(heightDescriptor.getName().id(), heightDescriptor.getName());
    cachedLabels.put(heightDescriptor.getDescription().id(), heightDescriptor.getDescription());
    Set<Object> cacheLookups = new LinkedHashSet<>();

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .using(labelCache(cachedLabels, cacheLookups))
      .findOne();

    assertThat(one).isNotNull();

    LoggedSql.start();
    assertThat(one.getName().version()).isGreaterThan(0);
    assertThat(one.getDescription().version()).isGreaterThan(0);
    List<String> sql = LoggedSql.stop();

    assertThat(cacheLookups)
      .containsExactlyInAnyOrder(heightDescriptor.getName().id(), heightDescriptor.getDescription().id());
    assertThat(sql).isEmpty();
  }

  @Test
  void find_usingImmutableLabelCacheWithPartialHits_expect_singleLazyLoadingQuery() {

    Map<Object, Label> cachedLabels = new LinkedHashMap<>();
    cachedLabels.put(heightDescriptor.getName().id(), heightDescriptor.getName());
    Set<Object> cacheLookups = new LinkedHashSet<>();

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .using(labelCache(cachedLabels, cacheLookups))
      .findOne();

    assertThat(one).isNotNull();

    LoggedSql.start();
    assertThat(one.getName().version()).isGreaterThan(0);
    assertThat(one.getDescription().version()).isGreaterThan(0);
    List<String> sql = LoggedSql.stop();

    assertThat(cacheLookups)
      .containsExactlyInAnyOrder(heightDescriptor.getName().id(), heightDescriptor.getDescription().id());
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("from label");
  }

  @Test
  void find_usingImmutableLabelCacheWithNoHits_expect_singleLazyLoadingQuery() {

    Set<Object> cacheLookups = new LinkedHashSet<>();

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .using(labelCache(new LinkedHashMap<>(), cacheLookups))
      .findOne();

    assertThat(one).isNotNull();

    LoggedSql.start();
    assertThat(one.getName().version()).isGreaterThan(0);
    assertThat(one.getDescription().version()).isGreaterThan(0);
    List<String> sql = LoggedSql.stop();

    assertThat(cacheLookups)
      .containsExactlyInAnyOrder(heightDescriptor.getName().id(), heightDescriptor.getDescription().id());
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("from label");
  }

  @Test
  void find_unmodifiableUsingImmutableLabelCache_expect_readableRefsWithNoLazyLoadSql() {

    Map<Object, Label> cachedLabels = new LinkedHashMap<>();
    cachedLabels.put(heightDescriptor.getName().id(), immutableLabel(heightDescriptor.getName().id()));
    cachedLabels.put(heightDescriptor.getDescription().id(), immutableLabel(heightDescriptor.getDescription().id()));
    Set<Object> cacheLookups = new LinkedHashSet<>();

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .setUnmodifiable(true)
      .using(labelCacheBackfill(cachedLabels, cacheLookups))
      .findOne();

    assertThat(one).isNotNull();
    assertThat(DB.beanState(one.getName()).isUnmodifiable()).isTrue();
    assertThat(DB.beanState(one.getDescription()).isUnmodifiable()).isTrue();

    LoggedSql.start();
    assertThat(one.getName().version()).isGreaterThan(0);
    assertThat(one.getDescription().version()).isGreaterThan(0);
    List<String> sql = LoggedSql.stop();

    assertThat(cacheLookups)
      .containsExactlyInAnyOrder(heightDescriptor.getName().id(), heightDescriptor.getDescription().id());
    assertThat(sql).isEmpty();
  }

  @Test
  void find_unmodifiableUsingImmutableLabelCacheWithPartialHits_expect_backfillAndNoLazyLoadSql() {

    Map<Object, Label> cachedLabels = new LinkedHashMap<>();
    cachedLabels.put(heightDescriptor.getName().id(), immutableLabel(heightDescriptor.getName().id()));
    Set<Object> cacheLookups = new LinkedHashSet<>();

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .setUnmodifiable(true)
      .using(labelCacheBackfill(cachedLabels, cacheLookups))
      .findOne();

    assertThat(one).isNotNull();
    assertThat(DB.beanState(one.getName()).isUnmodifiable()).isTrue();
    assertThat(DB.beanState(one.getDescription()).isUnmodifiable()).isTrue();

    LoggedSql.start();
    assertThat(one.getName().version()).isGreaterThan(0);
    assertThat(one.getDescription().version()).isGreaterThan(0);
    List<String> sql = LoggedSql.stop();

    assertThat(cacheLookups)
      .containsExactlyInAnyOrder(heightDescriptor.getName().id(), heightDescriptor.getDescription().id());
    assertThat(sql).isEmpty();
  }

  @Test
  void find_unmodifiableUsingImmutableLabelCacheWithNoHits_expect_backfillAndNoLazyLoadSql() {

    Set<Object> cacheLookups = new LinkedHashSet<>();

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .setUnmodifiable(true)
      .using(labelCacheBackfill(new LinkedHashMap<>(), cacheLookups))
      .findOne();

    assertThat(one).isNotNull();
    assertThat(DB.beanState(one.getName()).isUnmodifiable()).isTrue();
    assertThat(DB.beanState(one.getDescription()).isUnmodifiable()).isTrue();

    LoggedSql.start();
    assertThat(one.getName().version()).isGreaterThan(0);
    assertThat(one.getDescription().version()).isGreaterThan(0);
    List<String> sql = LoggedSql.stop();

    assertThat(cacheLookups)
      .containsExactlyInAnyOrder(heightDescriptor.getName().id(), heightDescriptor.getDescription().id());
    assertThat(sql).isEmpty();
  }

  private ImmutableBeanCache<Label> labelCache(Map<Object, Label> cachedLabels, Set<Object> cacheLookups) {
    return new ImmutableBeanCache<>() {
      @Override
      public Class<Label> type() {
        return Label.class;
      }

      @Override
      public Map<Object, Label> getAll(Set<Object> ids) {
        cacheLookups.addAll(ids);
        Map<Object, Label> hits = new LinkedHashMap<>();
        for (Object id : ids) {
          Label bean = cachedLabels.get(id);
          if (bean != null) {
            hits.put(id, bean);
          }
        }
        return hits;
      }
    };
  }

  private ImmutableBeanCache<Label> labelCacheBackfill(Map<Object, Label> cachedLabels, Set<Object> cacheLookups) {
    return new ImmutableBeanCache<>() {
      @Override
      public Class<Label> type() {
        return Label.class;
      }

      @Override
      public Map<Object, Label> getAll(Set<Object> ids) {
        cacheLookups.addAll(ids);
        Map<Object, Label> hits = new LinkedHashMap<>();
        for (Object id : ids) {
          Label bean = cachedLabels.get(id);
          if (bean == null) {
            bean = DB.find(Label.class).setId(id).setUnmodifiable(true).findOne();
            if (bean != null) {
              cachedLabels.put(id, bean);
            }
          }
          if (bean != null) {
            hits.put(id, bean);
          }
        }
        return hits;
      }
    };
  }

  private Label immutableLabel(Object id) {
    return DB.find(Label.class).setId(id).setUnmodifiable(true).findOne();
  }

}
