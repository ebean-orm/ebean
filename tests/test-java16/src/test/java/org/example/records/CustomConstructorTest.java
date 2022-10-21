package org.example.records;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomConstructorTest {

  @Test
  void nullInput() {
    assertThrows(NullPointerException.class, () -> new CustomConstructor(null, "b", "c"));
  }

  @Test
  void nullInputWithMessage() {
    assertThatThrownBy(() -> {
      new CustomConstructor("a", null, "c");
    }).isInstanceOf(NullPointerException.class)
      .hasMessageContaining("no line2");
  }

  @Test
  void alternateConstructor() {
    var bean = new CustomConstructor("a", "b");
    assertThat(bean.city()).isEqualTo("Auckland");
    assertThat(bean.line1()).isEqualTo("a");
    assertThat(bean.line2()).isEqualTo("b");
    assertThat(bean).isEqualTo(new CustomConstructor("a", "b", "Auckland"));
    assertThat(bean).isEqualTo(new CustomConstructor("a", "b"));

    assertThat(bean).isNotEqualTo(new CustomConstructor("a", "b", "Wellington"));
    assertThat(bean).isNotEqualTo(new CustomConstructor("z", "b", "Auckland"));
    assertThat(bean).isNotEqualTo(new CustomConstructor("a", "z", "Auckland"));
  }
}
