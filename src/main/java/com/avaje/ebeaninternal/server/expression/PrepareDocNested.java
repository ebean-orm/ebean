package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.Junction;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Prepare nested path expressions for
 */
class PrepareDocNested {

  /**
   * Prepare the top level expressions for nested path handling.
   */
  static void prepare(DefaultExpressionList<?> expressions, BeanDescriptor<?> beanDescriptor) {
    new PrepareDocNested(expressions, beanDescriptor, null).process();
  }

  /**
   * Prepare the Junction expressions for nested path handling.
   */
  static void prepare(DefaultExpressionList<?> expressions, BeanDescriptor<?> beanDescriptor, Junction.Type type) {
    new PrepareDocNested(expressions, beanDescriptor, type).process();
  }

  enum Mode {
    NONE,
    SINGLE,
    MIXED
  }

  private final Junction.Type type;
  private final DefaultExpressionList<?> original;
  private final BeanDescriptor<?> beanDescriptor;
  private final List<SpiExpression> origUnderlying;
  private final int origSize;

  private boolean hasNesting;
  private boolean hasMixedNesting;
  private String firstNestedPath;


  PrepareDocNested(DefaultExpressionList<?> original, BeanDescriptor<?> beanDescriptor, Junction.Type type) {
    this.type = type;
    this.beanDescriptor = beanDescriptor;
    this.original = original;
    this.origUnderlying = original.getUnderlyingList();
    this.origSize = origUnderlying.size();
  }

  void process() {

    PrepareDocNested.Mode mode = determineMode();
    if (mode == PrepareDocNested.Mode.SINGLE) {
      original.setAllDocNested(firstNestedPath);

    } else if (mode == PrepareDocNested.Mode.MIXED) {
      original.setUnderlying(group());
    }

  }

  /**
   * Reorganise the flat list of expressions into a tree grouping expressions by nested path.
   *
   * Returns the new top level list of expressions.
   */
  private List<SpiExpression> group() {

    Map<String,Group> groups = new LinkedHashMap<String,Group>();

    // organise expressions by nestedPath
    for (int i = 0; i < origSize; i++) {
      SpiExpression expr = origUnderlying.get(i);
      String nestedPath = expr.nestedPath(beanDescriptor);
      Group group = groups.get(nestedPath);
      if (group == null) {
        group = new Group(nestedPath);
        groups.put(nestedPath, group);
      }
      group.list.add(expr);
    }

    List<SpiExpression> newList = new ArrayList<SpiExpression>();
    Collection<Group> values = groups.values();
    for (Group group : values) {
      group.addTo(newList);
    }
    return newList;
  }

  /**
   * Determined the nested path mode.
   */
  private Mode determineMode() {

    if (!hasNesting()) {
      // no nested paths at all
      return Mode.NONE;
    }
    if (!hasMixedNesting) {
      // single nested path for all expressions
      return Mode.SINGLE;
    }
    // mixed nested paths to underlying expression list needs re-organising by nested path
    return Mode.MIXED;
  }

  /**
   * Return true if the expressions have nested paths.
   */
  private boolean hasNesting() {

    for (int i = 0; i < origSize; i++) {
      SpiExpression expr = origUnderlying.get(i);
      String nestedPath = expr.nestedPath(beanDescriptor);
      if (nestedPath == null) {
        hasMixedNesting = true;

      } if (nestedPath != null) {
        hasNesting = true;
        if (firstNestedPath == null) {
          firstNestedPath = nestedPath;
        } else if (hasMixedNesting || !firstNestedPath.equals(nestedPath)) {
          hasMixedNesting = true;
          return true;
        }
      }
    }

    return hasNesting;
  }


  /**
   * List of SpiExpression grouped by nested path.
   */
  class Group {

    final String nestedPath;

    final List<SpiExpression> list = new ArrayList<SpiExpression>();

    Group(String nestedPath) {
      this.nestedPath = nestedPath;
    }

    void addTo(List<SpiExpression> newList) {
      if (nestedPath == null) {
        newList.addAll(list);
      } else {
        newList.add(original.wrap(list, nestedPath, type));
      }
    }
  }

}
