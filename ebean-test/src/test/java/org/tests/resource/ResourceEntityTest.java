package org.tests.resource;

import io.ebean.DB;
import io.ebean.FetchGroup;
import io.ebean.ImmutableBeanCache;
import io.ebean.ImmutableBeanCaches;
import io.ebean.test.LoggedSql;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.TransactionEventTable;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.transaction.BeanPersistIds;
import io.ebeaninternal.server.transaction.RemoteTableMod;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.VwCustomer;

import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
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
  void find_mutableUsingImmutableBeanCache_expect_additionalLazyLoadingStillWorks() {

    var cache = loadingLabelCache();

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .using(cache)
      .findOne();

    assertThat(one).isNotNull();
    assertThat(DB.beanState(one.getName()).isUnmodifiable()).isFalse();
    assertThat(DB.beanState(one.getDescription()).isUnmodifiable()).isFalse();

    LoggedSql.start();
    assertThat(one.getName().version()).isGreaterThan(0);
    assertThat(one.getDescription().version()).isGreaterThan(0);
    List<String> sqlFromCache = LoggedSql.stop();
    assertThat(sqlFromCache).isEmpty();

    LoggedSql.start();
    assertThat(one.getName().getLabelTexts()).hasSize(2);
    assertThat(one.getDescription().getLabelTexts()).hasSize(2);
    List<String> lazySql = LoggedSql.stop();

    assertThat(lazySql.stream().anyMatch(sql -> sql.contains("from label_text"))).isTrue();
  }

  @Test
  void find_unmodifiableUsingImmutableLabelCache_expect_readableRefsWithNoLazyLoadSql() {

    var cache = loadingLabelCache();
    cache.getAll(Set.of(heightDescriptor.getName().id(), heightDescriptor.getDescription().id()));

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .setUnmodifiable(true)
      .using(cache)
      .findOne();

    assertThat(one).isNotNull();
    assertThat(DB.beanState(one.getName()).isUnmodifiable()).isTrue();
    assertThat(DB.beanState(one.getDescription()).isUnmodifiable()).isTrue();

    LoggedSql.start();
    assertThat(one.getName().version()).isGreaterThan(0);
    assertThat(one.getDescription().version()).isGreaterThan(0);
    List<String> sql = LoggedSql.stop();

    assertThat(sql).isEmpty();
  }

  @Test
  void find_unmodifiableUsingImmutableLabelCacheWithPartialHits_expect_backfillAndNoLazyLoadSql() {

    var cache = loadingLabelCache();
    cache.getAll(Set.of(heightDescriptor.getName().id()));

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .setUnmodifiable(true)
      .using(cache)
      .findOne();

    assertThat(one).isNotNull();
    assertThat(DB.beanState(one.getName()).isUnmodifiable()).isTrue();
    assertThat(DB.beanState(one.getDescription()).isUnmodifiable()).isTrue();

    LoggedSql.start();
    assertThat(one.getName().version()).isGreaterThan(0);
    assertThat(one.getDescription().version()).isGreaterThan(0);
    List<String> sql = LoggedSql.stop();

    assertThat(sql).isEmpty();
  }

  @Test
  void find_unmodifiableUsingImmutableLabelCacheWithNoHits_expect_backfillAndNoLazyLoadSql() {

    var cache = loadingLabelCache();

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .setUnmodifiable(true)
      .using(cache)
      .findOne();

    assertThat(one).isNotNull();
    assertThat(DB.beanState(one.getName()).isUnmodifiable()).isTrue();
    assertThat(DB.beanState(one.getDescription()).isUnmodifiable()).isTrue();

    LoggedSql.start();
    assertThat(one.getName().version()).isGreaterThan(0);
    assertThat(one.getDescription().version()).isGreaterThan(0);
    List<String> sql = LoggedSql.stop();

    assertThat(sql).isEmpty();
  }

  @Test
  void find_unmodifiableUsingImmutableBeanCachesLoading_expect_seededLabelsLoadedAndNoLazyLoadSql() {

    var cache = loadingLabelCache();

    LoggedSql.start();
    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .setUnmodifiable(true)
      .using(cache)
      .findOne();

    List<String> loadSql = LoggedSql.stop();

    assertThat(one).isNotNull();
    assertThat(one.getName().id()).isEqualTo(heightDescriptor.getName().id());
    assertThat(one.getDescription().id()).isEqualTo(heightDescriptor.getDescription().id());
    assertThat(DB.beanState(one.getName()).isUnmodifiable()).isTrue();
    assertThat(DB.beanState(one.getDescription()).isUnmodifiable()).isTrue();
    assertThat(loadSql.stream().filter(sql -> sql.contains(" from label "))).hasSize(1);

    LoggedSql.start();
    assertThat(one.getName().version()).isGreaterThan(0);
    assertThat(one.getDescription().version()).isGreaterThan(0);
    List<String> sql = LoggedSql.stop();
    assertThat(sql).isEmpty();
  }

  @Test
  void find_unmodifiableUsingImmutableBeanCachesLoadingWithLabelTexts_expect_assocLoadedAndNoLazyLoadSql() {

    var cache = loadingLabelWithTextsCache();

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .setUnmodifiable(true)
      .using(cache)
      .findOne();

    assertThat(one).isNotNull();
    assertThat(DB.beanState(one.getName()).isUnmodifiable()).isTrue();
    assertThat(DB.beanState(one.getDescription()).isUnmodifiable()).isTrue();

    LoggedSql.start();
    assertThat(one.getName().getLabelTexts())
      .extracting(LabelText::getLocaleText)
      .containsExactlyInAnyOrder("Height", "Höhe");

    assertThat(one.getDescription().getLabelTexts())
      .extracting(LabelText::getLocaleText)
      .containsExactlyInAnyOrder("Height property", "Höhe Eigenschaft");

    List<String> sql = LoggedSql.stop();
    assertThat(sql).isEmpty();
  }

  @Test
  void find_unmodifiableUsingImmutableBeanCachesBuilderLoadingWithLabelTexts_expect_assocLoadedAndNoLazyLoadSql() {

    FetchGroup<Label> fetchGroup = FetchGroup.of(Label.class)
      .select("version")
      .fetch("labelTexts", "locale, localeText")
      .build();

    ImmutableBeanCache<Label> cache = ImmutableBeanCaches.builder(Label.class)
      .loading(DB.getDefault(), fetchGroup)
      .maxSize(10_000)
      .maxIdleSeconds(300)
      .maxSecondsToLive(6_000)
      .build();

    AttributeDescriptor one = DB.find(AttributeDescriptor.class)
      .setId(heightDescriptor.id())
      .setUnmodifiable(true)
      .using(cache)
      .findOne();

    assertThat(one).isNotNull();
    assertThat(DB.beanState(one.getName()).isUnmodifiable()).isTrue();
    assertThat(DB.beanState(one.getDescription()).isUnmodifiable()).isTrue();

    LoggedSql.start();
    assertThat(one.getName().getLabelTexts())
      .extracting(LabelText::getLocaleText)
      .containsExactlyInAnyOrder("Height", "Höhe");

    assertThat(one.getDescription().getLabelTexts())
      .extracting(LabelText::getLocaleText)
      .containsExactlyInAnyOrder("Height property", "Höhe Eigenschaft");

    List<String> sql = LoggedSql.stop();
    assertThat(sql).isEmpty();
  }

  @Test
  void immutable_cache_insert_on_same_table_does_not_invalidate_existing_entries() {
    var cache = loadingLabelCache();
    Object existingId = heightDescriptor.getName().id();

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> initialLoadSql = LoggedSql.stop();
    assertThat(initialLoadSql.stream().filter(sql -> sql.contains(" from label "))).hasSize(1);

    Label inserted = new Label();
    inserted.addLabelText(Locale.ENGLISH, "Insert only");
    inserted.addLabelText(Locale.GERMAN, "Nur Insert");
    DB.save(inserted);

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> postInsertSql = LoggedSql.stop();
    assertThat(postInsertSql.stream().filter(sql -> sql.contains(" from label "))).isEmpty();
  }

  @Test
  void immutable_cache_sql_update_on_same_table_invalidates_existing_entries() {
    var cache = loadingLabelCache();
    Object existingId = heightDescriptor.getName().id();

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    LoggedSql.stop();

    DB.sqlUpdate("update label set version = version where id = ?")
      .setParameter(existingId)
      .execute();

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> postUpdateSql = LoggedSql.stop();
    assertThat(postUpdateSql.stream().filter(sql -> sql.contains(" from label "))).hasSize(1);
  }

  @Test
  void immutable_cache_remote_table_mod_on_same_table_invalidates_existing_entries() {
    var cache = loadingLabelCache();
    Object existingId = heightDescriptor.getName().id();

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    LoggedSql.stop();

    RemoteTransactionEvent remoteEvent = new RemoteTransactionEvent("other");
    remoteEvent.addRemoteTableMod(new RemoteTableMod(Set.of(labelTable())));
    spiServer().remoteTransactionEvent(remoteEvent);

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> postRemoteTableModSql = LoggedSql.stop();
    assertThat(postRemoteTableModSql.stream().filter(sql -> sql.contains(" from label "))).hasSize(1);
  }

  @Test
  void immutable_cache_remote_table_iud_insert_on_same_table_does_not_invalidate_existing_entries() {
    var cache = loadingLabelCache();
    Object existingId = heightDescriptor.getName().id();

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    LoggedSql.stop();

    RemoteTransactionEvent remoteEvent = new RemoteTransactionEvent("other");
    remoteEvent.addTableIUD(new TransactionEventTable.TableIUD(labelTable(), true, false, false));
    spiServer().remoteTransactionEvent(remoteEvent);

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> postRemoteInsertSql = LoggedSql.stop();
    assertThat(postRemoteInsertSql.stream().filter(sql -> sql.contains(" from label "))).isEmpty();
  }

  @Test
  void immutable_cache_remote_table_iud_update_on_same_table_invalidates_existing_entries() {
    var cache = loadingLabelCache();
    Object existingId = heightDescriptor.getName().id();

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    LoggedSql.stop();

    RemoteTransactionEvent remoteEvent = new RemoteTransactionEvent("other");
    remoteEvent.addTableIUD(new TransactionEventTable.TableIUD(labelTable(), false, true, false));
    spiServer().remoteTransactionEvent(remoteEvent);

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> postRemoteUpdateSql = LoggedSql.stop();
    assertThat(postRemoteUpdateSql.stream().filter(sql -> sql.contains(" from label "))).hasSize(1);
  }

  @Test
  void immutable_cache_remote_table_iud_delete_on_same_table_invalidates_existing_entries() {
    var cache = loadingLabelCache();
    Object existingId = heightDescriptor.getName().id();

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    LoggedSql.stop();

    RemoteTransactionEvent remoteEvent = new RemoteTransactionEvent("other");
    remoteEvent.addTableIUD(new TransactionEventTable.TableIUD(labelTable(), false, false, true));
    spiServer().remoteTransactionEvent(remoteEvent);

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> postRemoteDeleteSql = LoggedSql.stop();
    assertThat(postRemoteDeleteSql.stream().filter(sql -> sql.contains(" from label "))).hasSize(1);
  }

  @Test
  void immutable_cache_remote_delete_by_id_invalidates_only_target_id() {
    var cache = loadingLabelCache();
    Object removedId = heightDescriptor.getName().id();
    Object retainedId = heightDescriptor.getDescription().id();

    cache.getAll(Set.of(removedId, retainedId));

    RemoteTransactionEvent remoteEvent = new RemoteTransactionEvent("other");
    BeanPersistIds persistIds = new BeanPersistIds(spiServer().descriptor(Label.class));
    persistIds.addId(PersistRequest.Type.DELETE, removedId);
    remoteEvent.addBeanPersistIds(persistIds);
    spiServer().remoteTransactionEvent(remoteEvent);

    LoggedSql.start();
    cache.getAll(Set.of(removedId));
    List<String> removedSql = LoggedSql.stop();
    assertThat(removedSql.stream().filter(sql -> sql.contains(" from label "))).hasSize(1);

    LoggedSql.start();
    cache.getAll(Set.of(retainedId));
    List<String> retainedSql = LoggedSql.stop();
    assertThat(retainedSql.stream().filter(sql -> sql.contains(" from label "))).isEmpty();
  }

  @Test
  void immutable_cache_local_orm_update_on_label_invalidates_existing_entries() {
    Label label = createLabel("orm-update");
    var cache = loadingLabelCache();
    Object existingId = label.id();

    cache.getAll(Set.of(existingId));

    label.addLabelText(Locale.ENGLISH, "updated-" + UUID.randomUUID());
    DB.update(label);

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> postUpdateSql = LoggedSql.stop();
    assertThat(postUpdateSql.stream().filter(sql -> sql.contains(" from label "))).hasSize(1);
  }

  @Test
  void immutable_cache_local_orm_delete_on_label_invalidates_existing_entries() {
    Label label = createLabel("orm-delete");
    var cache = loadingLabelCache();
    Object existingId = label.id();

    cache.getAll(Set.of(existingId));
    DB.delete(label);

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> firstAfterDeleteSql = LoggedSql.stop();
    assertThat(firstAfterDeleteSql.stream().filter(sql -> sql.contains(" from label "))).hasSize(1);

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> secondAfterDeleteSql = LoggedSql.stop();
    assertThat(secondAfterDeleteSql.stream().filter(sql -> sql.contains(" from label "))).isEmpty();
  }

  @Test
  void immutable_cache_multiple_caches_for_same_type_are_both_invalidated_on_update() {
    var cacheA = loadingLabelCache();
    var cacheB = loadingLabelCache();
    Object existingId = heightDescriptor.getName().id();

    cacheA.getAll(Set.of(existingId));
    cacheB.getAll(Set.of(existingId));

    DB.sqlUpdate("update label set version = version where id = ?")
      .setParameter(existingId)
      .execute();

    LoggedSql.start();
    cacheA.getAll(Set.of(existingId));
    List<String> cacheASql = LoggedSql.stop();
    assertThat(cacheASql.stream().filter(sql -> sql.contains(" from label "))).hasSize(1);

    LoggedSql.start();
    cacheB.getAll(Set.of(existingId));
    List<String> cacheBSql = LoggedSql.stop();
    assertThat(cacheBSql.stream().filter(sql -> sql.contains(" from label "))).hasSize(1);
  }

  @Test
  void immutable_cache_view_invalidation_clears_on_dependent_table_touch() {
    Customer customerEntity = new Customer();
    customerEntity.setName("view-immutable-" + UUID.randomUUID().toString().substring(0, 8));
    customerEntity.setStatus(Customer.Status.ACTIVE);
    customerEntity.setAnniversary(new Date(System.currentTimeMillis()));
    DB.save(customerEntity);

    ImmutableBeanCache<VwCustomer> cache = ImmutableBeanCaches.loading(VwCustomer.class, DB.getDefault(), FetchGroup.of(VwCustomer.class, "name"));
    Object existingId = customerEntity.getId();

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> initialLoadSql = LoggedSql.stop();
    assertThat(initialLoadSql.stream().filter(sql -> sql.contains(" from o_customer "))).hasSize(1);

    DB.sqlUpdate("update o_customer set name = name where id = ?")
      .setParameter(existingId)
      .execute();

    LoggedSql.start();
    cache.getAll(Set.of(existingId));
    List<String> postTouchSql = LoggedSql.stop();
    assertThat(postTouchSql.stream().filter(sql -> sql.contains(" from o_customer "))).hasSize(1);
  }

  @Test
  void find_fetchQuerySecondary_inheritsImmutableCache_expect_noLabelLazyLoadSql() {

    var cache = loadingLabelCache();

    AttributeValueOwner owner = DB.find(AttributeValueOwner.class)
      .setId(resource.getAttributeValueOwner().id())
      .fetchQuery("attributeValues", "intValue")
      .fetch("attributeValues.attributeDescriptor", "name,description")
      .using(cache)
      .findOne();

    assertThat(owner).isNotNull();
    assertThat(owner.getAttributeValues()).hasSize(2);

    LoggedSql.start();
    accessDescriptorLabels(owner);
    List<String> sql = LoggedSql.stop();

    assertThat(sql).isEmpty();
  }

  @Test
  void find_fetchLazySecondary_inheritsImmutableCache_expect_noLabelLazyLoadSql() {

    var cache = loadingLabelCache();

    AttributeValueOwner owner = DB.find(AttributeValueOwner.class)
      .setId(resource.getAttributeValueOwner().id())
      .fetchLazy("attributeValues", "intValue")
      .fetch("attributeValues.attributeDescriptor", "name,description")
      .using(cache)
      .findOne();

    assertThat(owner).isNotNull();

    // Trigger the +lazy secondary query first.
    assertThat(owner.getAttributeValues()).hasSize(2);

    LoggedSql.start();
    accessDescriptorLabels(owner);
    List<String> sql = LoggedSql.stop();

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

  private ImmutableBeanCache<Label> loadingLabelCache() {
    return ImmutableBeanCaches.loading(Label.class, DB.getDefault(), FetchGroup.of(Label.class, "version"));
  }

  private ImmutableBeanCache<Label> loadingLabelWithTextsCache() {
    FetchGroup<Label> fetchGroup = FetchGroup.of(Label.class)
      .select("version")
      .fetch("labelTexts", "locale, localeText")
      .build();
    return ImmutableBeanCaches.loading(Label.class, DB.getDefault(), fetchGroup);
  }

  private SpiEbeanServer spiServer() {
    return (SpiEbeanServer) DB.getDefault();
  }

  private String labelTable() {
    return spiServer().descriptor(Label.class).baseTable();
  }

  private Label createLabel(String prefix) {
    Label label = new Label();
    label.addLabelText(Locale.ENGLISH, prefix + "-en");
    label.addLabelText(Locale.GERMAN, prefix + "-de");
    DB.save(label);
    return label;
  }

  private void accessDescriptorLabels(AttributeValueOwner owner) {
    for (AttributeValue attributeValue : owner.getAttributeValues()) {
      AttributeDescriptor descriptor = attributeValue.getAttributeDescriptor();
      assertThat(descriptor).isNotNull();
      assertThat(descriptor.getName().version()).isGreaterThan(0);
      assertThat(descriptor.getDescription().version()).isGreaterThan(0);
    }
  }

}
