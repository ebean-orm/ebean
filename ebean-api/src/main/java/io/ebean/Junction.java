package io.ebean;

/**
 * Represents a Conjunction or a Disjunction.
 * <p>
 * Basically with a Conjunction you join together many expressions with AND, and
 * with a Disjunction you join together many expressions with OR.
 * </p>
 * <p>
 * Note: where() always takes you to the top level WHERE expression list.
 * </p>
 * <pre>{@code
 * Query q =
 *     DB.find(Person.class)
 *       .where()
 *         .or()
 *           .like("name", "Rob%")
 *           .eq("status", Status.NEW)
 *
 *       // where() returns us to the top level expression list
 *       .where().gt("id", 10);
 *
 * // read as...
 * // where ( ((name like Rob%) or (status = NEW)) AND (id &gt; 10) )
 *
 * }</pre>
 * <p>
 * Note: endJunction() takes you to the parent expression list
 * </p>
 * <pre>{@code
 *
 * Query q =
 *     DB.find(Person.class)
 *       .where()
 *         .or()
 *           .like("name", "Rob%")
 *           .eq("status", Status.NEW)
 *           .endJunction()
 *
 *           // endJunction().. takes us to the 'parent' expression list
 *           // which in this case is the top level (same as where())
 *
 *         .gt("id", 10);
 *
 * // read as...
 * // where ( ((name like Rob%) or (status = NEW)) AND (id > 10) )
 * }</pre>
 * <p>
 * Example of a nested disjunction.
 * </p>
 * <pre>{@code
 * Query<Customer> q =
 *  DB.find(Customer.class)
 *      .where()
 *        .or()
 *          .and()
 *            .startsWith("name", "r")
 *            .eq("anniversary", onAfter)
 *            .endAnd()
 *          .and()
 *            .eq("status", Customer.Status.ACTIVE)
 *            .gt("id", 0)
 *            .endAnd()
 *      .orderBy().asc("name");
 *
 * q.findList();
 * String s = q.getGeneratedSql();
 *
 *  // this produces an expression like:
 *  ( name like ? and c.anniversary = ? ) or (c.status = ?  and c.id > ? )
 *
 * }</pre>
 */
public interface Junction<T> extends Expression, ExpressionList<T> {

  /**
   * The type of Junction used in full text expressions.
   */
  enum Type {

    /**
     * AND group.
     */
    AND(" and ", "", false),

    /**
     * OR group.
     */
    OR(" or ", "", false),

    /**
     * NOT group.
     */
    NOT(" and ", "not ", false),

    /**
     * FILTER group (for internal use only).
     */
    FILTER("filter", "", true),

    /**
     * Text search AND group.
     */
    MUST("must", "", true),

    /**
     * Text search NOT group.
     */
    MUST_NOT("must_not", "", true),

    /**
     * Text search OR group.
     */
    SHOULD("should", "", true);

    private final String prefix;
    private final String literal;
    private final boolean text;

    Type(String literal, String prefix, boolean text) {
      this.literal = literal;
      this.prefix = prefix;
      this.text = text;
    }

    /**
     * Return the literal value for this type.
     */
    public String literal() {
      return literal;
    }

    /**
     * Return the prefix value for this type.
     */
    public String prefix() {
      return prefix;
    }

    /**
     * Return true if this is a text type.
     */
    public boolean isText() {
      return text;
    }

  }

}
