package org.tests.json;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;
import org.tests.model.json.EBasicOldValue;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class TestJsonNullValues extends BaseTestCase {

  @Test
  public void testSetToNull() {
    EBasicOldValue bean = new EBasicOldValue();
    DB.save(bean);
    bean = DB.find(EBasicOldValue.class, bean.getId());

    bean.setStringList(null);
    bean.setStringSet(null);
    bean.setObjectMap(null);
    bean.setLongList(null);
    bean.setLongSet(null);
    bean.setLongMap(null);
    bean.setIntList(null);
    bean.setIntSet(null);
    bean.setIntMap(null);

    DB.save(bean);
    bean = DB.find(EBasicOldValue.class, bean.getId());

    assertThat(bean.getStringList()).isEmpty();
    assertThat(bean.getStringSet()).isEmpty();
    assertThat(bean.getObjectMap()).isEmpty();
    assertThat(bean.getLongList()).isEmpty();
    assertThat(bean.getLongSet()).isEmpty();
    assertThat(bean.getLongMap()).isEmpty();
    assertThat(bean.getIntList()).isEmpty();
    assertThat(bean.getIntSet()).isEmpty();
    assertThat(bean.getIntMap()).isEmpty();
  }

  @Test
  public void testSetOneToNullAnotherToEmpty() {
    EBasicOldValue bean = new EBasicOldValue();
    DB.save(bean);
    bean = DB.find(EBasicOldValue.class, bean.getId());

    bean.setStringList(null);
    bean.setStringSet(null);

    DB.save(bean);
    bean = DB.find(EBasicOldValue.class, bean.getId());

    bean.setStringList(new ArrayList<>());
    bean.setStringSet(null);

    DB.save(bean);
    bean = DB.find(EBasicOldValue.class, bean.getId());

    assertThat(bean.getStringList()).isEmpty();
    assertThat(bean.getStringSet()).isEmpty();
  }

}
