package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.CacheMode;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;

import org.tests.model.basic.EBasicVer;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class TestQueryCacheReadOnly extends BaseTestCase {

  @Test
  public void testReadOnly() {

    EbeanServer server = Ebean.getServer(null);
    EBasicVer account = new EBasicVer("an other junk");
    server.save(account);
    
    Query<EBasicVer> baseQuery = server.find(EBasicVer.class).setUseQueryCache(CacheMode.ON);

    List<EBasicVer> alist = baseQuery.findList();
    assertThat(alist).isNotEmpty();
    assertThatThrownBy(alist::clear).hasMessageContaining("This collection is in ReadOnly mode");
    alist = baseQuery.findList();
    assertThat(alist).isNotEmpty();
    assertThatThrownBy(alist::clear).hasMessageContaining("This collection is in ReadOnly mode");

    Map<String,EBasicVer> amap = baseQuery.setMapKey("name").findMap();
    assertThat(amap).isNotEmpty();
    assertThatThrownBy(amap::clear).hasMessageContaining("This collection is in ReadOnly mode");
    amap = baseQuery.setMapKey("name").findMap();
    assertThat(amap).isNotEmpty();
    assertThatThrownBy(amap::clear).hasMessageContaining("This collection is in ReadOnly mode");

    Set<EBasicVer> aset = baseQuery.findSet();
    assertThat(aset).isNotEmpty();
    assertThatThrownBy(aset::clear).hasMessageContaining("This collection is in ReadOnly mode");
    aset = baseQuery.findSet();
    assertThat(aset).isNotEmpty();
    assertThatThrownBy(aset::clear).hasMessageContaining("This collection is in ReadOnly mode");

    // we will get an unmodifiable collection here
    List<Object> attributeList = baseQuery.select("name").findSingleAttributeList();
    assertThat(attributeList).isNotEmpty();
    assertThatThrownBy(attributeList::clear).isInstanceOf(UnsupportedOperationException.class);
    attributeList = baseQuery.select("name").findSingleAttributeList();
    assertThat(attributeList).isNotEmpty();
    assertThatThrownBy(attributeList::clear).isInstanceOf(UnsupportedOperationException.class);

    List<Object> idList = baseQuery.select("name").findIds();
    assertThat(idList).isNotEmpty();
    assertThatThrownBy(idList::clear).isInstanceOf(UnsupportedOperationException.class);
    idList = baseQuery.select("name").findIds();
    assertThat(idList).isNotEmpty();
    assertThatThrownBy(idList::clear).isInstanceOf(UnsupportedOperationException.class);
  }


  @Test
  public void testNotReadOnly() {

    EbeanServer server = Ebean.getServer(null);
    EBasicVer account = new EBasicVer("an other junk");
    server.save(account);

    Query<EBasicVer> baseQuery = server.find(EBasicVer.class).setUseQueryCache(CacheMode.ON).setReadOnly(false);

    List<EBasicVer> alist = baseQuery.findList();
    assertThat(alist).isNotEmpty();
    alist.clear();
    alist = baseQuery.findList();
    assertThat(alist).isNotEmpty();
    alist.clear();

    Map<String,EBasicVer> amap = baseQuery.setMapKey("name").findMap();
    assertThat(amap).isNotEmpty();
    amap.clear(); 
    amap = baseQuery.setMapKey("name").findMap();
    assertThat(amap).isNotEmpty();
    amap.clear(); 

    Set<EBasicVer> aset = baseQuery.findSet();
    assertThat(aset).isNotEmpty();
    aset.clear();
    aset = baseQuery.findSet();
    assertThat(aset).isNotEmpty();
    aset.clear();

    List<Object> attributeList = baseQuery.select("name").findSingleAttributeList();
    assertThat(attributeList).isNotEmpty();
    attributeList.clear();
    attributeList = baseQuery.select("name").findSingleAttributeList();
    assertThat(attributeList).isNotEmpty();
    attributeList.clear();

    List<Object> idList = baseQuery.select("name").findIds();
    assertThat(idList).isNotEmpty();
    idList.clear();
    idList = baseQuery.select("name").findIds();
    assertThat(idList).isNotEmpty();
    idList.clear();
  }
}
