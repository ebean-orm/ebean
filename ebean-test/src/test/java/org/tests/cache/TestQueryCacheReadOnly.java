package org.tests.cache;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestQueryCacheReadOnly extends BaseTestCase {

  @Test
  public void testReadOnly() {

    Database server = DB.getDefault();
    EBasicVer account = new EBasicVer("an other junk");
    server.save(account);

    Query<EBasicVer> baseQuery = server.find(EBasicVer.class).setUseQueryCache(CacheMode.ON);

    List<EBasicVer> alist = baseQuery.findList();
    assertThat(alist).isNotEmpty();
    assertThatThrownBy(alist::clear).isInstanceOf(UnsupportedOperationException.class);
    alist = baseQuery.findList();
    assertThat(alist).isNotEmpty();
    assertThatThrownBy(alist::clear).isInstanceOf(UnsupportedOperationException.class);

    Map<String,EBasicVer> amap = baseQuery.setMapKey("name").findMap();
    assertThat(amap).isNotEmpty();
    assertThatThrownBy(amap::clear).isInstanceOf(UnsupportedOperationException.class);
    amap = baseQuery.setMapKey("name").findMap();
    assertThat(amap).isNotEmpty();
    assertThatThrownBy(amap::clear).isInstanceOf(UnsupportedOperationException.class);

    Set<EBasicVer> aset = baseQuery.findSet();
    assertThat(aset).isNotEmpty();
    assertThatThrownBy(aset::clear).isInstanceOf(UnsupportedOperationException.class);
    aset = baseQuery.findSet();
    assertThat(aset).isNotEmpty();
    assertThatThrownBy(aset::clear).isInstanceOf(UnsupportedOperationException.class);

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

    Database server = DB.getDefault();
    EBasicVer account = new EBasicVer("an other junk");
    server.save(account);

    Query<EBasicVer> baseQuery = server.find(EBasicVer.class).setUseQueryCache(CacheMode.ON);

    List<EBasicVer> alist = baseQuery.findList();
    assertThat(alist).isNotEmpty();
    assertThatThrownBy(alist::clear).isInstanceOf(UnsupportedOperationException.class);
    alist = baseQuery.findList();
    assertThat(alist).isNotEmpty();
    assertThatThrownBy(alist::clear).isInstanceOf(UnsupportedOperationException.class);

    Map<String,EBasicVer> amap = baseQuery.setMapKey("name").findMap();
    assertThat(amap).isNotEmpty();
    assertThatThrownBy(amap::clear).isInstanceOf(UnsupportedOperationException.class);
    amap = baseQuery.setMapKey("name").findMap();
    assertThat(amap).isNotEmpty();
    assertThatThrownBy(amap::clear).isInstanceOf(UnsupportedOperationException.class);

    Set<EBasicVer> aset = baseQuery.findSet();
    assertThat(aset).isNotEmpty();
    assertThatThrownBy(aset::clear).isInstanceOf(UnsupportedOperationException.class);
    aset = baseQuery.findSet();
    assertThat(aset).isNotEmpty();
    assertThatThrownBy(aset::clear).isInstanceOf(UnsupportedOperationException.class);

    final List<Object> attributeList = baseQuery.select("name").findSingleAttributeList();
    assertThat(attributeList).isNotEmpty();
    assertThatThrownBy(attributeList::clear).isInstanceOf(UnsupportedOperationException.class);
    final List<Object> attributeList2 = baseQuery.select("name").findSingleAttributeList();
    assertThat(attributeList2).isNotEmpty();
    assertThatThrownBy(attributeList2::clear).isInstanceOf(UnsupportedOperationException.class);

    List<Object> idList = baseQuery.select("name").findIds();
    assertThat(idList).isNotEmpty();
    assertThatThrownBy(idList::clear).isInstanceOf(UnsupportedOperationException.class);
    List<Object> idList2 = baseQuery.select("name").findIds();
    assertThat(idList2).isNotEmpty();
    assertThatThrownBy(idList2::clear).isInstanceOf(UnsupportedOperationException.class);
  }
}
