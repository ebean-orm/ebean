package org.tests.json;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.ValuePair;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.text.TextException;

import org.assertj.core.api.SoftAssertions;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Ignore;
import org.junit.Test;
import org.tests.model.json.EBasicJsonList;
import org.tests.model.json.EBasicOldValue;
import org.tests.model.json.PlainBean;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestOldValue extends BaseTestCase {

  
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public void testDbJsonOldValue() throws Exception {
    EBasicOldValue bean = new EBasicOldValue();

    bean.getStringList().add("sl1");
    bean.getStringSet().add("ss1");
    bean.getObjectMap().put("sk1","sm1");
    bean.getLongList().add(1L);
    bean.getLongSet().add(1001L);
    bean.getLongMap().put("lk1",2001L);
    bean.getIntList().add(2);
    bean.getIntSet().add(1002);
    bean.getIntMap().put("ik1",2002);

    DB.save(bean);
    bean = DB.find(EBasicOldValue.class, bean.getId());

    bean.getStringList().add("sl2");
    bean.getStringSet().add("ss2");
    bean.getObjectMap().put("sk2","sm2");
    bean.getLongList().add(5L);
    bean.getLongSet().add(1005L);
    bean.getLongMap().put("lk2",2005L);
    bean.getIntList().add(6);
    bean.getIntSet().add(1006);
    bean.getIntMap().put("ik2",2006);

    
    Map<String, ValuePair> dirty = DB.getBeanState(bean).getDirtyValues();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(dirty).hasSize(9);

    softly.assertThat((List)dirty.get("stringList").getOldValue()).containsExactly("sl1");
    softly.assertThat((List)dirty.get("stringList").getNewValue()).containsExactly("sl1", "sl2");
    softly.assertThat((List)dirty.get("longList").getOldValue()).containsExactly(1L);
    softly.assertThat((List)dirty.get("longList").getNewValue()).containsExactly(1L, 5L);
    softly.assertThat((List)dirty.get("intList").getOldValue()).containsExactly(2);
    softly.assertThat((List)dirty.get("intList").getNewValue()).containsExactly(2, 6);

    softly.assertThat((Set)dirty.get("stringSet").getOldValue()).containsExactly("ss1");
    softly.assertThat((Set)dirty.get("stringSet").getNewValue()).containsExactly("ss1", "ss2");
    softly.assertThat((Set)dirty.get("longSet").getOldValue()).containsExactly(1001L);
    softly.assertThat((Set)dirty.get("longSet").getNewValue()).containsExactly(1001L, 1005L);
    softly.assertThat((Set)dirty.get("intSet").getOldValue()).containsExactly(1002);
    softly.assertThat((Set)dirty.get("intSet").getNewValue()).containsExactly(1002, 1006);

    softly.assertThat((Map)dirty.get("objectMap").getOldValue()).containsEntry("sk1","sm1").hasSize(1);
    softly.assertThat((Map)dirty.get("objectMap").getNewValue()).containsEntry("sk1","sm1").containsEntry("sk2","sm2").hasSize(2);
    softly.assertThat((Map)dirty.get("longMap").getOldValue()).containsEntry("lk1",2001L).hasSize(1);
    softly.assertThat((Map)dirty.get("longMap").getNewValue()).containsEntry("lk1",2001L).containsEntry("lk2",2005L).hasSize(2);
    softly.assertThat((Map)dirty.get("intMap").getOldValue()).containsEntry("ik1",2002).hasSize(1);
    softly.assertThat((Map)dirty.get("intMap").getNewValue()).containsEntry("ik1",2002).containsEntry("ik2",2006).hasSize(2);

    softly.assertAll();

  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  @Ignore("Old value detection does not work for @DbArray")
  public void testDbArrayOldValue() throws Exception {
    EBasicOldValue bean = new EBasicOldValue();

    bean.getStringArr().add("sa1");
  

    DB.save(bean);
    bean = DB.find(EBasicOldValue.class, bean.getId());

    bean.getStringArr().add("sa2");
    

    
    Map<String, ValuePair> dirty = DB.getBeanState(bean).getDirtyValues();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(dirty).hasSize(1);

    softly.assertThat((List)dirty.get("stringArr").getOldValue()).containsExactly("sa1");
    softly.assertThat((List)dirty.get("stringArr").getNewValue()).containsExactly("sa1", "sa2");

    softly.assertAll();

  }
}
