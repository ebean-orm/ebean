package io.ebeaninternal.server.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DtoMetaBuilderTest {

  @Test
  public void includeMethod() {
    Map<String, Method> methods = getIncludedMethodsFor(D0.class);

    assertThat(methods).hasSize(2);
    assertThat(methods.get("setName")).isNotNull();
    assertThat(methods.get("setId")).isNotNull();
  }

  @Test
  public void includeMethod_when_notStrictlySetters() {
    Map<String, Method> methods = getIncludedMethodsFor(D1.class);

    assertThat(methods).hasSize(3);
    assertThat(methods.get("setNameThen")).isNotNull();
    assertThat(methods.get("setIdFor")).isNotNull();
    assertThat(methods.get("setI")).isNotNull();
  }

  @Test
  public void propertyType() {
    Map<String, Method> methods = getIncludedMethodsFor(D0.class);

    assertThat(methods).hasSize(2);
    assertThat(DtoMetaProperty.propertyClass(methods.get("setName"))).isEqualTo(String.class);
    assertThat(DtoMetaProperty.propertyClass(methods.get("setId"))).isEqualTo(long.class);
    assertThat(DtoMetaProperty.propertyType(methods.get("setName"))).isEqualTo(String.class);
    assertThat(DtoMetaProperty.propertyType(methods.get("setId"))).isEqualTo(long.class);
  }

  @Test
  public void propertyName() {

    assertThat(DtoMetaBuilder.propertyName("setName")).isEqualTo("name");
    assertThat(DtoMetaBuilder.propertyName("setId")).isEqualTo("id");
    assertThat(DtoMetaBuilder.propertyName("setI")).isEqualTo("i");
    assertThat(DtoMetaBuilder.propertyName("setfoo")).isEqualTo("foo");
  }


  private Map<String, Method> getIncludedMethodsFor(Class<?> cls) {
    Map<String,Method> included = new HashMap<>();
    for (Method method : cls.getMethods()) {
     if (DtoMetaBuilder.includeMethod(method)) {
       included.put(method.getName(), method);
     }
    }
    return included;
  }

  @SuppressWarnings("unused")
  static class D0 {
    private String name;
    private long id;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setId(long id) {
      this.id = id;
    }

    public void setNamePlus(String name, long id) {
      this.name = name;
      this.id = id;
    }

    public static void setFoo(String foo) {
    }

    protected void setProtected(String foo) {
    }

    private void setPrivate(String foo) {
    }

    private void setPackage(String foo) {
    }
  }


  @SuppressWarnings("unused")
  static class D1 {

    public void setNameThen(String name) {

    }

    public void setIdFor(long id) {

    }

    public void setI(long val) {

    }

    public void set(long val) {

    }

    public D1 setA(long val) {
      return this;
    }
  }
}
