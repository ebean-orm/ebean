package io.ebean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Holds a list of value object pairs.
 * <p>
 * This feature is to enable use of L2 cache with complex natural keys with findList() queries in cases where the
 * IN clause is not a single property but instead a pair of properties.
 * <p>
 * These queries can have predicates that can be translated into a list of complex natural keys such that the L2
 * cache can be hit with these keys to obtain some or all of the beans from L2 cache rather than the DB.
 *
 * <pre>{@code
 *
 *   // where a bean is annotated with a complex
 *   // natural key made of several properties
 *   @Cache(naturalKey = {"store","code","sku"})
 *
 *
 *   Pairs pairs = new Pairs("sku", "code");
 *   pairs.add("sj2", 1000);
 *   pairs.add("sj2", 1001);
 *   pairs.add("pf3", 1000);
 *
 *   List<OCachedNatKeyBean3> list = DB.find(OCachedNatKeyBean3.class)
 *   .where()
 *   .eq("store", "def")
 *   .inPairs(pairs)       // IN clause with 'pairs' of values
 *   .orderBy("sku desc")
 *
 *   // query expressions cover the natural key properties
 *   // so we can choose to hit the L2 bean cache if we want
 *   .setUseCache(true)
 *   .findList();
 *
 * }</pre>
 * <h3>Important implementation Note</h3>
 * <p>
 * When binding many pairs of values we want to be able to utilise a DB index (as this type of query usually means the
 * pairs are a unique key/index or part of a unique key/index and highly selective). Currently we know we can do this
 * on any DB that supports expression/formula based indexes.
 * using a DB string concatenation formula
 * <p>
 * This means, the implementation converts the list of pairs into a list of strings via concatenation and we use a
 * DB concatenation formula to match. We see SQL like:
 *
 * <pre>{@code sql
 *
 *   ...
 *   where t0.store = ?  and (t0.sku||'-'||t0.code) in (?, ? )
 *
 *   // bind values like: "sj2-1000", "pf3-1000"
 *
 * }</pre>
 * <p>
 * We often create a DB expression index to match the DB concat formula like:
 *
 * <pre>{@code sql
 *
 *   create index ix_name on table_name (sku || '-' || code);
 *
 * }</pre>
 */
public final class Pairs {

  private final String property0;
  private final String property1;
  private final List<Entry> entries = new ArrayList<>();

  /**
   * Character between the values when combined via DB varchar concatenation.
   */
  private String concatSeparator = "-";

  /**
   * Optional suffix added to DB varchar concatenation formula.
   */
  private String concatSuffix;

  /**
   * Create with 2 property names.
   *
   * @param property0 The property of the first value
   * @param property1 The property of the second value
   */
  public Pairs(String property0, String property1) {
    this.property0 = property0;
    this.property1 = property1;
  }

  /**
   * Add a pair of value objects.
   * <p>
   * Both values are expected to be immutable with equals and hashCode implementations.
   * </p>
   *
   * @param a Value of the first property
   * @param b Value of the second property
   */
  public Pairs add(Object a, Object b) {
    entries.add(new Entry(a, b));
    return this;
  }

  /**
   * Return the first property name.
   */
  public String property0() {
    return property0;
  }

  /**
   * Return the second property name.
   */
  public String property1() {
    return property1;
  }

  /**
   * Return all the value pairs.
   */
  public List<Entry> entries() {
    return Collections.unmodifiableList(entries);
  }

  /**
   * Return the separator character used with DB varchar concatenation to combine the 2 values.
   */
  public String concatSeparator() {
    return concatSeparator;
  }

  /**
   * Set the separator character used with DB varchar concatenation to combine the 2 values.
   */
  public Pairs concatSeparator(String concatSeparator) {
    this.concatSeparator = concatSeparator;
    return this;
  }

  /**
   * Return  a suffix used with DB varchar concatenation to combine the 2 values.
   */
  public String concatSuffix() {
    return concatSuffix;
  }

  /**
   * Add a suffix used with DB varchar concatenation to combine the 2 values.
   */
  public Pairs concatSuffix(String concatSuffix) {
    this.concatSuffix = concatSuffix;
    return this;
  }

  @Override
  public String toString() {
    return "p0:" + property0 + " p1:" + property1 + " entries:" + entries;
  }

  /**
   * A pair of 2 value objects.
   * <p>
   * Used to support inPairs() expression.
   */
  public static class Entry {

    private final Object a;
    private final Object b;

    /**
     * Create with values for property0 and property1 respectively.
     *
     * @param a Value of the first property
     * @param b Value of the second property
     */
    public Entry(Object a, Object b) {
      this.a = a;
      this.b = b;
    }

    @Override
    public String toString() {
      return "{" + a + "," + b + "}";
    }

    /**
     * Return the value for the first property.
     */
    public Object getA() {
      return a;
    }

    /**
     * Return the value for the second property.
     */
    public Object getB() {
      return b;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Entry that = (Entry) o;
      return a.equals(that.a) && b.equals(that.b);
    }

    @Override
    public int hashCode() {
      return Objects.hash(a, b);
    }
  }
}
