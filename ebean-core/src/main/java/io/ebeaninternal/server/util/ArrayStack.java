package io.ebeaninternal.server.util;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

/**
 * Stack based on ArrayList.
 *
 * @author rbygrave
 */
public class ArrayStack<E> {

  private final List<E> list;

  /**
   * Creates an empty Stack with an initial size.
   */
  public ArrayStack(int size) {
    this.list = new ArrayList<>(size);
  }

  /**
   * Creates an empty Stack.
   */
  public ArrayStack() {
    this.list = new ArrayList<>();
  }

  @Override
  public String toString() {
    return list.toString();
  }

  /**
   * Pushes an item onto the top of this stack.
   */
  public void push(E item) {
    list.add(item);
  }

  /**
   * Removes the object at the top of this stack and returns that object as
   * the value of this function.
   */
  public E pop() {
    int len = list.size();
    if (len == 0) {
      throw new EmptyStackException();
    }
    return list.remove(len - 1);
  }

  private E peekZero(boolean retNull) {
    int len = list.size();
    if (len == 0) {
      if (retNull) {
        return null;
      }
      throw new EmptyStackException();
    }
    return list.get(len - 1);
  }

  /**
   * Returns the object at the top of this stack without removing it.
   */
  public E peek() {
    return peekZero(false);
  }

  /**
   * Returns the object at the top of this stack without removing it.
   * If the stack is empty this returns null.
   */
  public E peekWithNull() {
    return peekZero(true);
  }

  /**
   * Tests if this stack is empty.
   */
  public boolean isEmpty() {
    return list.isEmpty();
  }

  public int size() {
    return list.size();
  }

  public boolean contains(E o) {
    return list.contains(o);
  }
}
