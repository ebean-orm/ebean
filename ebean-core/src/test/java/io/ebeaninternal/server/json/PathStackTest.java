package io.ebeaninternal.server.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PathStackTest {

  @Test
  void peekFullPath() {
    var pathStack = new PathStack();
    assertThat(pathStack.peekFullPath("foo")).isEqualTo("foo");

    pathStack.push("one");
    assertThat(pathStack.peekFullPath("foo")).isEqualTo("one.foo");
    pathStack.push("two");
    assertThat(pathStack.peekFullPath("foo")).isEqualTo("two.foo");

    assertThat(pathStack.pop()).isEqualTo("two");
    assertThat(pathStack.peekFullPath("foo")).isEqualTo("one.foo");
    assertThat(pathStack.pop()).isEqualTo("one");
    assertThat(pathStack.peekFullPath("foo")).isEqualTo("foo");
  }

  @Test
  void pushPathKey() {
    var pathStack = new PathStack();
    assertThat(pathStack.peekFullPath("foo")).isEqualTo("foo");
    pathStack.pushPathKey("one");
    assertThat(pathStack.peekFullPath("foo")).isEqualTo("one.foo");
    pathStack.pushPathKey("two");
    assertThat(pathStack.peekFullPath("foo")).isEqualTo("one.two.foo");
    pathStack.pop();
    assertThat(pathStack.peekFullPath("foo")).isEqualTo("one.foo");
    pathStack.pop();
    assertThat(pathStack.peekFullPath("foo")).isEqualTo("foo");
  }
}
