package io.ebean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an Order By for a Query.
 * <p>
 * Is an ordered list of OrderBy.Property objects each specifying a property and
 * whether it is ascending or descending order.
 * <p>
 * Typically, you will not construct an OrderBy yourself but use one that exists
 * on the Query object.
 */
public class OrderBy<T> implements Serializable {

  private static final long serialVersionUID = 9157089257745730539L;

  private transient Query<T> query;

  private final List<Property> list;

  /**
   * Create an OrderBy parsing the given order by clause.
   * <p>
   * The order by clause follows SQL order by clause with comma's between each
   * property and optionally "asc" or "desc" to represent ascending or
   * descending order respectively.
   */
  public static <P> OrderBy<P> of(String orderByClause) {
    return new OrderBy<>(orderByClause);
  }

  /**
   * @deprecated This method will be removed from public API.
   * <p>
   * Create an empty OrderBy with no associated query.
   */
  @Deprecated(forRemoval = true)
  public OrderBy() {
    this.list = new ArrayList<>(3);
  }

  private OrderBy(List<Property> list) {
    this.list = list;
  }

  /**
   * @deprecated migrate to {@link OrderBy#of(String)}.
   */
  @Deprecated(forRemoval = true)
  public OrderBy(String orderByClause) {
    this(null, orderByClause);
  }

  /**
   * @deprecated This method will be removed from public API.
   */
  @Deprecated(forRemoval = true)
  public OrderBy(Query<T> query, String orderByClause) {
    this.query = query;
    this.list = new ArrayList<>(3);
    parse(orderByClause);
  }

  /**
   * Reverse the ascending/descending order on all the properties.
   */
  public void reverse() {
    for (Property aList : list) {
      aList.reverse();
    }
  }

  /**
   * Add a property with ascending order to this OrderBy.
   */
  public Query<T> asc(String propertyName) {
    list.add(new Property(propertyName, true));
    return query;
  }

  /**
   * Add a property with ascending order to this OrderBy.
   */
  public Query<T> asc(String propertyName, String collation) {
    list.add(new Property(propertyName, true, collation));
    return query;
  }

  /**
   * Add a property with descending order to this OrderBy.
   */
  public Query<T> desc(String propertyName) {
    list.add(new Property(propertyName, false));
    return query;
  }

  /**
   * Add a property with descending order to this OrderBy.
   */
  public Query<T> desc(String propertyName, String collation) {
    list.add(new Property(propertyName, false, collation));
    return query;
  }

  /**
   * Return true if the property is known to be contained in the order by clause.
   */
  public boolean containsProperty(String propertyName) {
    for (Property aList : list) {
      if (propertyName.equals(aList.getProperty())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @deprecated This method will become internal only API.
   * <p>
   * Return a copy of this OrderBy with the path trimmed.
   */
  @Deprecated(forRemoval = true)
  public OrderBy<T> copyWithTrim(String path) {
    List<Property> newList = new ArrayList<>(list.size());
    for (Property aList : list) {
      newList.add(aList.copyWithTrim(path));
    }
    return new OrderBy<>(newList);
  }

  /**
   * Return the properties for this OrderBy.
   */
  public List<Property> getProperties() {
    // not returning an Immutable list at this point
    return list;
  }

  /**
   * Return true if this OrderBy does not have any properties.
   */
  public boolean isEmpty() {
    return list.isEmpty();
  }

  /**
   * Return the associated query if there is one.
   */
  public Query<T> getQuery() {
    return query;
  }

  /**
   * Associate this OrderBy with a query.
   */
  public void setQuery(Query<T> query) {
    this.query = query;
  }

  /**
   * Return a copy of the OrderBy.
   */
  public OrderBy<T> copy() {
    OrderBy<T> copy = new OrderBy<>();
    for (Property property : list) {
      copy.add(property.copy());
    }
    return copy;
  }

  /**
   * Add to the order by by parsing a raw expression.
   */
  public void add(String rawExpression) {
    parse(rawExpression);
  }

  /**
   * Add a property to the order by.
   */
  public void add(Property p) {
    list.add(p);
  }

  @Override
  public String toString() {
    return list.toString();
  }

  /**
   * Returns the OrderBy in string format.
   */
  public String toStringFormat() {
    if (list.isEmpty()) {
      return null;
    }
    var append = new StringAppend();
    for (int i = 0; i < list.size(); i++) {
      Property property = list.get(i);
      if (i > 0) {
        append.append(", ");
      }
      property.toStringFormat(append);
    }
    return append.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof OrderBy<?>)) {
      return false;
    }
    OrderBy<?> e = (OrderBy<?>) obj;
    return e.list.equals(list);
  }

  /**
   * Return a hash value for this OrderBy. This can be to determine logical
   * equality for OrderBy clauses.
   */
  @Override
  public int hashCode() {
    return list.hashCode();
  }

  /**
   * Clear the orderBy removing any current order by properties.
   * <p>
   * This is intended to be used when some code creates a query with a
   * 'default' order by clause and some other code may clear the 'default'
   * order by clause and replace.
   * </p>
   */
  public OrderBy<T> clear() {
    list.clear();
    return this;
  }

  /**
   * Append the order by clause.
   */
  public interface Append {

    /**
     * Append a property expression.
     */
    Append property(String property);

    /**
     * Append a literal.
     */
    Append append(String literal);

    /**
     * Parse and append an expression.
     */
    Append parse(String expression);
  }

  private static final class StringAppend implements Append {

    private final StringBuilder builder = new StringBuilder();

    @Override
    public String toString() {
      return builder.toString();
    }

    @Override
    public Append property(String property) {
      builder.append(property);
      return this;
    }

    @Override
    public Append append(String literal) {
      builder.append(literal);
      return this;
    }

    @Override
    public Append parse(String raw) {
      builder.append(raw);
      return this;
    }
  }

  /**
   * A property and its ascending descending order.
   */
  public static class Property implements Serializable {

    private static final long serialVersionUID = 1546009780322478077L;

    private String property;

    private boolean ascending;

    private String collation;

    private String nulls;

    private String highLow;

    public Property(String property, boolean ascending) {
      this.property = property;
      this.ascending = ascending;
    }

    public Property(String property, boolean ascending, String nulls, String highLow) {
      this.property = property;
      this.ascending = ascending;
      this.nulls = nulls;
      this.highLow = highLow;
    }

    public Property(String property, boolean ascending, String collation) {
      this.property = property;
      this.ascending = ascending;
      this.collation = collation;
    }

    public Property(String property, boolean ascending, String collation, String nulls, String highLow) {
      this.property = property;
      this.ascending = ascending;
      this.collation = collation;
      this.nulls = nulls;
      this.highLow = highLow;
    }

    /**
     * Return a copy of this Property with the path trimmed.
     */
    public Property copyWithTrim(String path) {
      return new Property(property.substring(path.length() + 1), ascending, collation, nulls, highLow);
    }

    @Override
    public int hashCode() {
      int hc = property.hashCode();
      hc = hc * 92821 + (ascending ? 0 : 1);
      hc = hc * 92821 + (collation == null ? 0 : collation.hashCode());
      hc = hc * 92821 + (nulls == null ? 0 : nulls.hashCode());
      hc = hc * 92821 + (highLow == null ? 0 : highLow.hashCode());
      return hc;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof Property)) {
        return false;
      }
      Property e = (Property) obj;
      if (ascending != e.ascending) return false;
      if (!property.equals(e.property)) return false;
      if (!Objects.equals(collation, e.collation)) return false;
      if (!Objects.equals(nulls, e.nulls)) return false;
      return Objects.equals(highLow, e.highLow);
    }

    @Override
    public String toString() {
      return property;
    }

    public void toStringFormat(Append append) {
      if (collation != null)  {
        if (collation.contains("${}")) {
          // this is a complex collation, e.g. DB2 - we must replace the property
          append.parse(collation.replace("${}", property));
        } else {
          append.property(property).append(" collate ").append(collation);
        }
      } else {
        append.property(property);
      }
      if (!ascending) {
        append.append(" desc");
      }
      if (nulls != null) {
        append.append(" ").append(nulls).append(" ").append(highLow);
      }
    }

    /**
     * Reverse the ascending/descending order for this property.
     */
    public void reverse() {
      this.ascending = !ascending;
    }

    /**
     * Trim off the pathPrefix.
     */
    public void trim(String pathPrefix) {
      property = property.substring(pathPrefix.length() + 1);
    }

    /**
     * Return a copy of this property.
     */
    public Property copy() {
      return new Property(property, ascending, collation, nulls, highLow);
    }

    /**
     * Return the property name.
     */
    public String getProperty() {
      return property;
    }

    /**
     * Set the property name.
     */
    public void setProperty(String property) {
      this.property = property;
    }

    /**
     * Return true if the order is ascending.
     */
    public boolean isAscending() {
      return ascending;
    }

    /**
     * Set to true if the order is ascending.
     */
    public void setAscending(boolean ascending) {
      this.ascending = ascending;
    }

  }

  private void parse(String orderByClause) {
    if (orderByClause == null) {
      return;
    }
    for (String chunk : orderByClause.split(",")) {
      Property p = parseProperty(chunk);
      if (p != null) {
        list.add(p);
      }
    }
  }

  private Property parseProperty(String chunk) {
    String[] pairs = chunk.split(" ");
    if (pairs.length == 0) {
      return null;
    }

    ArrayList<String> wordList = new ArrayList<>(pairs.length);
    for (String pair : pairs) {
      if (!isEmptyString(pair)) {
        wordList.add(pair);
      }
    }
    if (wordList.isEmpty()) {
      return null;
    }
    if (wordList.size() == 1) {
      return new Property(wordList.get(0), true);
    }
    if (wordList.size() == 2) {
      boolean asc = isAscending(wordList.get(1));
      return new Property(wordList.get(0), asc);
    }
    if (wordList.size() == 4) {
      // nulls high or nulls low as 3rd and 4th
      boolean asc = isAscending(wordList.get(1));
      return new Property(wordList.get(0), asc, wordList.get(2), wordList.get(3));
    }
    return new Property(chunk.trim(), true);
  }

  private boolean isAscending(String s) {
    s = s.toLowerCase();
    if (s.startsWith("asc")) {
      return true;
    }
    if (s.startsWith("desc")) {
      return false;
    }
    throw new RuntimeException("Expecting [" + s + "] to be asc or desc?");
  }

  private boolean isEmptyString(String s) {
    return s == null || s.isEmpty();
  }
}
