package org.tests.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.DB;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.json.BasicJacksonType;
import org.tests.model.json.EBasicJsonJackson;
import org.tests.model.json.EBasicJsonJackson2;
import org.tests.model.json.LongJacksonType;
import org.tests.model.json.StringJacksonType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDbJson_Jackson extends BaseTestCase {


  /**
   * This testcase verifies if the &#64;JsonDeserialize Annotation will work properly in ebean.
   */
  @Test
  public void testJsonDeserializeAnnotation() throws IOException {

    EBasicJsonJackson bean = new EBasicJsonJackson();

    bean.setName("stuff");

    bean.setPlainValue("plain");

    bean.getValueSet().add("A");
    bean.getValueSet().add("B");

    bean.getValueList().add("A");
    bean.getValueList().add("B");

    bean.getValueMap().put(1, "one");
    bean.getValueMap().put(2, "two");

    ObjectMapper mapper = (ObjectMapper) DB.getDefault().pluginApi().config().getObjectMapper();

    String json = mapper.writeValueAsString(bean);
    EBasicJsonJackson found = mapper.readValue(json, EBasicJsonJackson.class);

    // here we assert, that we can serialize/deserialize that bean with jackson
    assertThat(found.getPlainValue()).isEqualTo("plain");
    assertThat(found.getValueList()).containsExactly("A", "B");
    assertThat(found.getValueSet()).containsExactly("A", "B");
    assertThat(found.getValueMap()).containsEntry(1L, "one").containsEntry(2L, "two");

    DB.save(bean);

    // here we do the same and expect, that we can deserialize the bean also
    found = DB.find(EBasicJsonJackson.class, bean.getId());

    assertThat(found.getPlainValue()).isEqualTo("plain");
    assertThat(found.getValueList()).containsExactly("A", "B");
    assertThat(found.getValueSet()).containsExactly("A", "B");
    assertThat(found.getValueMap()).containsEntry(1L, "one").containsEntry(2L, "two");
  }

  public static class DtoJackson {
    @DbJson(length = 700)
    Set<BasicJacksonType<?>> valueSet = new LinkedHashSet<>();

    @DbJsonB
    List<BasicJacksonType<?>> valueList = new ArrayList<>();

    @DbJson(length = 700)
    @JsonDeserialize(keyAs = Long.class)
    Map<Number, BasicJacksonType<?>> valueMap = new LinkedHashMap<>();

    @DbJson(length = 500)
    BasicJacksonType<?> plainValue;

    public Set<BasicJacksonType<?>> getValueSet() {
      return valueSet;
    }

    public void setValueSet(Set<BasicJacksonType<?>> valueSet) {
      this.valueSet = valueSet;
    }

    public List<BasicJacksonType<?>> getValueList() {
      return valueList;
    }

    public void setValueList(List<BasicJacksonType<?>> valueList) {
      this.valueList = valueList;
    }

    public Map<Number, BasicJacksonType<?>> getValueMap() {
      return valueMap;
    }

    public void setValueMap(Map<Number, BasicJacksonType<?>> valueMap) {
      this.valueMap = valueMap;
    }

    public BasicJacksonType<?> getPlainValue() {
      return plainValue;
    }

    public void setPlainValue(BasicJacksonType<?> plainValue) {
      this.plainValue = plainValue;
    }
  }

  /**
   * This testcase verifies if polymorph objects will work in ebean.
   * <p>
   * for BasicJacksonType there exists two types and has a &#64;JsonTypeInfo
   * annotation. It is expected that this information is also honored by ebean.
   */
  @Test
  public void testPolymorph() throws IOException {

    EBasicJsonJackson2 bean = new EBasicJsonJackson2();

    bean.setName("stuff");

    bean.setPlainValue(new LongJacksonType(42L));

    bean.getValueSet().add(new StringJacksonType("A"));
    bean.getValueSet().add(new LongJacksonType(7l));

    bean.getValueList().add(new StringJacksonType("A"));
    bean.getValueList().add(new LongJacksonType(7l));

    bean.getValueMap().put(1, new StringJacksonType("A"));
    bean.getValueMap().put(2, new LongJacksonType(7l));

    ObjectMapper mapper = (ObjectMapper) DB.getDefault().pluginApi().config().getObjectMapper();

    String json = mapper.writeValueAsString(bean);
    assertThat(json)
      .contains("\"type\":\"string\"")
      .contains("\"type\":\"long\"");
    EBasicJsonJackson2 found = mapper.readValue(json, EBasicJsonJackson2.class);

    assertThat(found.getPlainValue()).isInstanceOf(LongJacksonType.class);
    assertThat(found.getValueList()).hasSize(2);
    assertThat(found.getValueSet()).hasSize(2);
    assertThat(found.getValueMap()).hasSize(2);
    ;

    DB.save(bean);

    found = DB.find(EBasicJsonJackson2.class, bean.getId());

    assertThat(found.getPlainValue()).isInstanceOf(LongJacksonType.class);
    assertThat(found.getValueList()).hasSize(2);
    assertThat(found.getValueSet()).hasSize(2);
    assertThat(found.getValueMap()).hasSize(2);


    DtoJackson dto = DB.find(EBasicJsonJackson2.class).setId(bean.getId())
      .select("valueSet,valueList,valueMap,plainValue").asDto(DtoJackson.class).findOne();

    assertThat(dto.getPlainValue()).isInstanceOf(LongJacksonType.class);
    assertThat(dto.getValueList()).hasSize(2);
    assertThat(dto.getValueSet()).hasSize(2);
    assertThat(dto.getValueMap()).hasSize(2);

  }

}
