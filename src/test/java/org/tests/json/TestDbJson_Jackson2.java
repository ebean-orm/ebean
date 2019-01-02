package org.tests.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;
import org.tests.model.json.EBasicJsonJackson2;
import org.tests.model.json.LongJacksonType;
import org.tests.model.json.StringJacksonType;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDbJson_Jackson2 extends BaseTestCase {

  private EBasicJsonJackson2 bean = new EBasicJsonJackson2();

  private EBasicJsonJackson2 found;

  @Test
  public void insert() throws IOException {

    bean.setName("stuff");

    bean.setPlainValue(new LongJacksonType(42L));

    bean.getValueSet().add(new StringJacksonType("A"));
    bean.getValueSet().add(new LongJacksonType(7l));

    bean.getValueList().add(new StringJacksonType("A"));
    bean.getValueList().add(new LongJacksonType(7l));

    bean.getValueMap().put(1, new StringJacksonType("A"));
    bean.getValueMap().put(2, new LongJacksonType(7l));

    ObjectMapper mapper = (ObjectMapper) Ebean.getDefaultServer().getPluginApi().getServerConfig().getObjectMapper();

    String json = mapper.writeValueAsString(bean);
    found = mapper.readValue(json, EBasicJsonJackson2.class);

    assertThat(found.getPlainValue()).isInstanceOf(LongJacksonType.class);
    assertThat(found.getValueList()).hasSize(2);
    assertThat(found.getValueSet()).hasSize(2);
    assertThat(found.getValueMap()).hasSize(2);;

    Ebean.save(bean);

    found = Ebean.find(EBasicJsonJackson2.class, bean.getId());

    assertThat(found.getPlainValue()).isInstanceOf(LongJacksonType.class);
    assertThat(found.getValueList()).hasSize(2);
    assertThat(found.getValueSet()).hasSize(2);
    assertThat(found.getValueMap()).hasSize(2);;

  }


}
