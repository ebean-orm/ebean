package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebeaninternal.server.deploy.BeanProperty;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class GeneratedCounterLongTest {

  private final GeneratedCounterLong counter = new GeneratedCounterLong();

  @Test
  void when_null_expect_IllegalStateException() {
    BeanProperty beanProperty = mock(BeanProperty.class);
    when(beanProperty.getValue(any())).thenReturn(null);

    assertThrows(IllegalStateException.class, () -> counter.getUpdateValue(beanProperty, null, System.currentTimeMillis()));
  }

  @Test
  void when_set_expect_incremented() {
    BeanProperty beanProperty = mock(BeanProperty.class);
    when(beanProperty.getValue(any())).thenReturn(7L);

    Object value = counter.getUpdateValue(beanProperty, null, System.currentTimeMillis());
    assertThat(value).isEqualTo(8L);
  }

}
