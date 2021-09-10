package io.ebeaninternal.server.util;

import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ArrayStackTest {

  @Test
  public void testPushPop() {
    ArrayStack<String> stack = new ArrayStack<>();
    stack.push("1");
    stack.push("2");
    stack.push("3");

    assertThat(stack.pop()).isEqualTo("3");
    assertThat(stack.pop()).isEqualTo("2");
    assertThat(stack.pop()).isEqualTo("1");
  }

  @Test
  public void testPushPop_given_stackInitialSizeExceeded() {
    ArrayStack<String> stack = new ArrayStack<>(2);
    stack.push("1");
    stack.push("2");
    stack.push("3");

    assertThat(stack.pop()).isEqualTo("3");
    assertThat(stack.pop()).isEqualTo("2");
    assertThat(stack.pop()).isEqualTo("1");
  }

  @Test
  public void testPop_given_emptyStack_throws() {
    ArrayStack<String> stack = new ArrayStack<>();
    assertThrows(EmptyStackException.class, stack::pop);
  }

  @Test
  public void testPeek_given_empty_throws() {
    ArrayStack<String> stack = new ArrayStack<>();
    assertThrows(EmptyStackException.class, stack::peek);
  }

  @Test
  public void testPeek_given_notEmpty() {
    ArrayStack<String> stack = new ArrayStack<>();
    stack.push("1");
    assertThat(stack.peek()).isEqualTo("1");
  }

  @Test
  public void testPeekWithNull() {
    ArrayStack<String> stack = new ArrayStack<>();
    assertThat(stack.peekWithNull()).isNull();

    stack.push("1");
    assertThat(stack.peekWithNull()).isEqualTo("1");
  }

  @Test
  public void testIsEmpty() {
    ArrayStack<String> stack = new ArrayStack<>();
    assertThat(stack.isEmpty()).isTrue();

    stack.push("1");
    assertThat(stack.isEmpty()).isFalse();
  }

  @Test
  public void testSize() {
    ArrayStack<String> stack = new ArrayStack<>();
    assertThat(stack.size()).isEqualTo(0);

    stack.push("1");
    assertThat(stack.size()).isEqualTo(1);
    stack.push("w");
    assertThat(stack.size()).isEqualTo(2);
  }

  @Test
  public void testContains() {
    ArrayStack<String> stack = new ArrayStack<>();
    stack.push("1");

    assertThat(stack.contains("1")).isTrue();
    assertThat(stack.contains("2")).isFalse();
  }
}
