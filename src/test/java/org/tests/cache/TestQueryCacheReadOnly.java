package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.CacheMode;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.tests.model.basic.EBasicVer;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class TestQueryCacheReadOnly extends BaseTestCase {

  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);
    EBasicVer account = new EBasicVer("an other junk");
    server.save(account);

    List<EBasicVer> alist = server.find(EBasicVer.class).setUseQueryCache(CacheMode.ON).findList();
    assertThatThrownBy(alist::clear).hasMessageContaining("This collection is in ReadOnly mode");
    
    Map<String,EBasicVer> amap = server.find(EBasicVer.class).setUseQueryCache(CacheMode.ON).setMapKey("name").findMap();
    assertThatThrownBy(amap::clear).hasMessageContaining("This collection is in ReadOnly mode");
    
    Set<EBasicVer> aset = server.find(EBasicVer.class).setUseQueryCache(CacheMode.ON).findSet();
    assertThatThrownBy(aset::clear).hasMessageContaining("This collection is in ReadOnly mode");
    
    List<Object> attributeList = server.find(EBasicVer.class).setUseQueryCache(CacheMode.ON).select("name").findSingleAttributeList();
    // we will get an unmodifiable collection here
    assertThatThrownBy(attributeList::clear).isInstanceOf(UnsupportedOperationException.class);
    
    List<Object> idList = server.find(EBasicVer.class).setUseQueryCache(CacheMode.ON).select("name").findIds();
    assertThatThrownBy(idList::clear).isInstanceOf(UnsupportedOperationException.class);
  }
}
