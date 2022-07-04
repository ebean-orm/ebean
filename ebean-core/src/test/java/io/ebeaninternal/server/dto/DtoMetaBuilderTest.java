package io.ebeaninternal.server.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DtoMetaBuilderTest {

  @Test
  void includeMethod() {
    Map<String, Method> methods = getIncludedMethodsFor(D0.class);

    assertThat(methods).hasSize(2);
    assertThat(methods).containsKeys("setId", "setName");
  }

  @Test
  void includeMethod_when_notStrictlySetters() {
    Map<String, Method> methods = getIncludedMethodsFor(D1.class);

    assertThat(methods).hasSize(5);
    assertThat(methods).containsKeys("setNameThen", "setIdFor", "setI", "setA", "set");
  }

  @Test
  void includeMethod_when_fluidAccessors() {
    Map<String, Method> methods = getIncludedMethodsFor(D2FluidAccessors.class);

    assertThat(methods).hasSize(5);
    assertThat(methods).containsKeys("nameThen", "idFor", "a", "i", "set");
  }

  @Test
  void includeMethod_when_plainAccessors() {
    Map<String, Method> methods = getIncludedMethodsFor(D2PlainAccessors.class);

    assertThat(methods).hasSize(5);
    assertThat(methods).containsKeys("nameThen", "idFor", "a", "i", "set");
  }

  @Test
  void propertyType() {
    Map<String, Method> methods = getIncludedMethodsFor(D0.class);

    assertThat(methods).hasSize(2);
    assertThat(DtoMetaProperty.propertyClass(methods.get("setName"))).isEqualTo(String.class);
    assertThat(DtoMetaProperty.propertyClass(methods.get("setId"))).isEqualTo(long.class);
    assertThat(DtoMetaProperty.propertyType(methods.get("setName"))).isEqualTo(String.class);
    assertThat(DtoMetaProperty.propertyType(methods.get("setId"))).isEqualTo(long.class);
  }

  @Test
  void propertyName() {
    assertThat(DtoMetaBuilder.propertyName("setName")).isEqualTo("name");
    assertThat(DtoMetaBuilder.propertyName("setId")).isEqualTo("id");
    assertThat(DtoMetaBuilder.propertyName("setI")).isEqualTo("i");
    assertThat(DtoMetaBuilder.propertyName("setFoo")).isEqualTo("foo");
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

  @SuppressWarnings("unused")
  static class D2FluidAccessors {

    public D2FluidAccessors nameThen(String name) {
      return this;
    }

    public D2FluidAccessors idFor(long id) {
      return this;
    }

    public D2FluidAccessors i(long val) {
      return this;
    }

    public D2FluidAccessors set(long val) {
      return this;
    }

    public D2FluidAccessors a(long val) {
      return this;
    }
  }

  @SuppressWarnings("unused")
  static class D2PlainAccessors {

    public void nameThen(String name) {
    }

    public void idFor(long id) {
    }

    public void i(long val) {
    }

    public void set(long val) {
    }

    public void a(long val) {
    }
  }
}
