package io.ebean.querybean.generator;

final class KotlinLangAdapter {

  void alias(Append writer, String shortName, String fullName) {
    writer.append("  companion object {").eol();
    writer.append("    /**").eol();
    writer.append("     * shared 'Alias' instance used to provide").eol();
    writer.append("     * properties to select and fetch clauses").eol();
    writer.append("     */").eol();
    writer.append("    val _alias = Q").append(shortName).append("(true)").eol();
    writer.eol();
    writer.append("    /**").eol();
    writer.append("     * Return a query bean used to build a FetchGroup.").eol();
    writer.append("     */").eol();
    writer.append("    fun forFetchGroup(): Q%s {", shortName).eol();
    writer.append("      return Q%s(io.ebean.FetchGroup.queryFor(%s::class.java));", shortName, fullName).eol();
    writer.append("    }").eol();
    writer.append("  }").eol().eol();
  }

  void rootBeanConstructor(Append writer, String shortName, String dbName, String fullName) {
    String name = (dbName == null) ? "default" : dbName;
    writer.append("  /**  Construct using the %s Database. */", name).eol();
    if (dbName == null) {
      writer.append("  constructor() : super(%s::class.java)", fullName).eol().eol();
    } else {
      writer.append("  constructor() : super(%s::class.java, io.ebean.DB.byName(\"%s\"))", fullName, dbName).eol().eol();
    }

    writer.append("  /**  @deprecated migrate to query.usingTransaction() */", name).eol();
    writer.append("  @Deprecated(message=\"migrate to query.usingTransaction()\")").eol();
    if (dbName == null) {
      writer.append("  constructor(transaction: io.ebean.Transaction) : super(%s::class.java, transaction)", fullName).eol().eol();
    } else {
      writer.append("  constructor(transaction: io.ebean.Transaction) : super(%s::class.java, io.ebean.DB.byName(\"%s\"), transaction)", fullName, dbName).eol().eol();
    }

    writer.append("  /**  Construct with a given Database. */").eol();
    writer.append("  constructor(database: io.ebean.Database) : super(%s::class.java, database)", fullName).eol().eol();

    writer.append("  /** Construct for Alias. */").eol();
    writer.append("  private constructor(dummy: Boolean) : super(dummy)").eol().eol();

    writer.append("  /** Private constructor for FetchGroup building. */").eol();
    writer.append("  private constructor(fetchGroupQuery: io.ebean.Query<%s>) : super(fetchGroupQuery)", fullName).eol().eol();

    writer.append("   /** Private constructor for filterMany  */").eol();
    writer.append("  private constructor(filter: io.ebean.ExpressionList<%s>) : super(filter)", fullName).eol().eol();

    writer.append("  /** Return a copy of the query. */").eol();
    writer.append("  override fun copy() : Q%s {", shortName).eol();
    writer.append("    return Q%s(query().copy())", shortName).eol();
    writer.append("  }").eol().eol();
  }
}
