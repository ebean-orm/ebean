package io.ebean.querybean.generator;


import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A simple implementation that generates and writes query beans.
 */
class SimpleQueryBeanWriter {

  private final Set<String> importTypes = new TreeSet<>();
  private final List<PropertyMeta> properties = new ArrayList<>();
  private final TypeElement element;
  private final TypeElement implementsInterface;
  private String implementsInterfaceFullName;
  private String implementsInterfaceShortName;
  private final ProcessingContext processingContext;
  private final String dbName;
  private final String beanFullName;
  private final boolean isEntity;
  private final boolean embeddable;

  private String destPackage;
  private String shortName;
  private final String shortInnerName;
  private final String origShortName;
  private Append writer;

  SimpleQueryBeanWriter(TypeElement element, ProcessingContext processingContext) {
    this.element = element;
    this.processingContext = processingContext;
    this.beanFullName = element.getQualifiedName().toString();
    boolean nested = element.getNestingKind().isNested();
    this.destPackage = Util.packageOf(nested, beanFullName) + ".query";
    String sn = Util.shortName(nested, beanFullName);
    this.shortInnerName = Util.shortName(false, sn);
    this.shortName = sn.replace('.', '$');
    this.origShortName = shortName;
    this.isEntity = processingContext.isEntity(element);
    this.embeddable = processingContext.isEmbeddable(element);
    this.dbName = findDbName();
    this.implementsInterface = initInterface(element);
  }

  private TypeElement initInterface(TypeElement element) {
    for (TypeMirror anInterface : element.getInterfaces()) {
      TypeElement e = (TypeElement)processingContext.asElement(anInterface);
      String name = e.getQualifiedName().toString();
      if (!name.startsWith("java") && !name.startsWith("io.ebean")) {
        return e;
      }
    }
    return null;
  }

  private String findDbName() {
    return processingContext.findDbName(element);
  }

  private boolean isEntity() {
    return isEntity;
  }

  private boolean isEmbeddable() {
    return embeddable;
  }

  private void gatherPropertyDetails() {
    importTypes.add(Constants.GENERATED);
    importTypes.add(beanFullName);
    importTypes.add(Constants.TYPEQUERYBEAN);
    if (embeddable) {
      importTypes.add(Constants.TQASSOC);
    } else {
      importTypes.add(Constants.TQASSOCBEAN);
      importTypes.add(Constants.TQROOTBEAN);
      importTypes.add(Constants.DATABASE);
      importTypes.add(Constants.FETCHGROUP);
      importTypes.add(Constants.QUERY);
      importTypes.add(Constants.TRANSACTION);
      importTypes.add(Constants.CONSUMER);
      importTypes.add(Constants.EXPR);
      importTypes.add(Constants.EXPRESSIONLIST);
    }

    if (implementsInterface != null) {
      implementsInterfaceFullName = implementsInterface.getQualifiedName().toString();
      boolean nested = implementsInterface.getNestingKind().isNested();
      implementsInterfaceShortName = Util.shortName(nested, implementsInterfaceFullName);

      importTypes.add(Constants.AVAJE_LANG_NULLABLE);
      importTypes.add(Constants.JAVA_COLLECTION);
      importTypes.add(implementsInterfaceFullName);
    }
    if (dbName != null) {
      importTypes.add(Constants.DB);
    }
    addClassProperties();
  }

  /**
   * Recursively add properties from the inheritance hierarchy.
   * <p>
   * Includes properties from mapped super classes and usual inheritance.
   */
  private void addClassProperties() {
    for (VariableElement field : processingContext.allFields(element)) {
      PropertyType type = processingContext.getPropertyType(field);
      if (type != null) {
        type.addImports(importTypes);
        properties.add(new PropertyMeta(field.getSimpleName().toString(), type));
      }
    }
  }

  /**
   * Write the type query bean (root bean).
   */
  void writeRootBean() throws IOException {
    gatherPropertyDetails();
    processingContext.addEntity(beanFullName, dbName);
    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClass();
    if (isEntity()) {
      writeAlias();
      writeFields();
      writeConstructors();
      writeStaticAliasClass();
    }
    writeAssocClass();
    writeClassEnd();
    writer.close();
  }

  /**
   * Write constructors.
   */
  private void writeConstructors() {
    writer.eol();
    writer.append("  /**").eol();
    writer.append("   * Return a query bean used to build a FetchGroup.").eol();
    writer.append("   * <p>").eol();
    writer.append("   * FetchGroups are immutable and threadsafe and can be used by many").eol();
    writer.append("   * concurrent queries. We typically stored FetchGroup as a static final field.").eol();
    writer.append("   * <p>").eol();
    writer.append("   * Example creating and using a FetchGroup.").eol();
    writer.append("   * <pre>{@code").eol();
    writer.append("   * ").eol();
    writer.append("   * static final FetchGroup<Customer> fetchGroup = ").eol();
    writer.append("   *   QCustomer.forFetchGroup()").eol();
    writer.append("   *     .shippingAddress.fetch()").eol();
    writer.append("   *     .contacts.fetch()").eol();
    writer.append("   *     .buildFetchGroup();").eol();
    writer.append("   * ").eol();
    writer.append("   * List<Customer> customers = new QCustomer()").eol();
    writer.append("   *   .select(fetchGroup)").eol();
    writer.append("   *   .findList();").eol();
    writer.append("   * ").eol();
    writer.append("   * }</pre>").eol();
    writer.append("   */").eol();
    writer.append("  public static Q%s forFetchGroup() {", shortName).eol();
    writer.append("    return new Q%s(FetchGroup.queryFor(%s.class));", shortName, shortName).eol();
    writer.append("  }").eol();
    writer.eol();

    String name = (dbName == null) ? "default" : dbName;
    writer.append("  /** Construct using the %s Database */", name).eol();
    writer.append("  public Q%s() {", shortName).eol();
    if (dbName == null) {
      writer.append("    super(%s.class);", shortName).eol();
    } else {
      writer.append("    super(%s.class, DB.byName(\"%s\"));", shortName, dbName).eol();
    }
    writer.append("  }").eol();
    writer.eol();

    writer.append("  /** Construct with a given transaction */").eol();
    writer.append("  public Q%s(Transaction transaction) {", shortName).eol();
    if (dbName == null) {
      writer.append("    super(%s.class, transaction);", shortName).eol();
    } else {
      writer.append("    super(%s.class, DB.byName(\"%s\"), transaction);", shortName, dbName).eol();
    }
    writer.append("  }").eol();

    writer.eol();
    writer.append("  /** Construct with a given Database */").eol();
    writer.append("  public Q%s(Database database) {", shortName).eol();
    writer.append("    super(%s.class, database);", shortName).eol();
    writer.append("  }").eol();
    writer.eol();

    writer.eol();
    writer.append("  /** Private constructor for Alias */").eol();
    writer.append("  private Q%s(boolean dummy) {", shortName).eol();
    writer.append("    super(dummy);").eol();
    writer.append("  }").eol();

    writer.eol();
    writer.append("  /** Private constructor for FetchGroup building */").eol();
    writer.append("  private Q%s(Query<%s> fetchGroupQuery) {", shortName, shortName).eol();
    writer.append("    super(fetchGroupQuery);").eol();
    writer.append("  }").eol();

    writer.eol();
    writer.append("  /** Private constructor for filterMany */").eol();
    writer.append("  private Q%s(ExpressionList<%s> filter) {", shortName, shortName).eol();
    writer.append("    super(filter);").eol();
    writer.append("  }").eol();
  }

  /**
   * Write all the fields.
   */
  private void writeFields() {
    for (PropertyMeta property : properties) {
      property.writeFieldDefn(writer, shortName, false);
      writer.eol();
    }
    writer.eol();
  }

  private void writeClass() {
    writer.append("/**").eol();
    writer.append(" * Query bean for %s.", shortName).eol();
    writer.append(" * ").eol();
    writer.append(" * THIS IS A GENERATED OBJECT, DO NOT MODIFY THIS CLASS.").eol();
    writer.append(" */").eol();
    writer.append(Constants.AT_GENERATED).eol();
    if (embeddable) {
      writer.append("public final class Q%s {", shortName).eol();
    } else {
      writer.append(Constants.AT_TYPEQUERYBEAN).eol();
      writer.append("public final class Q%s extends TQRootBean<%1$s,Q%1$s> {", shortName).eol();
    }
    writer.eol();
  }

  private void writeAlias() {
    writer.append("  private static final Q%s _alias = new Q%1$s(true);", shortName).eol().eol();

    writer.append("  /**").eol();
    writer.append("   * Return the shared 'Alias' instance used to provide properties to ").eol();
    writer.append("   * <code>select()</code> and <code>fetch()</code> ").eol();
    writer.append("   */").eol();
    writer.append("  public static Q%s alias() {", shortName).eol();
    writer.append("    return _alias;").eol();
    writer.append("  }").eol();
    writer.eol();
  }

  private void writeStaticAliasClass() {
    writer.eol();
    writer.append("  /**").eol();
    writer.append("   * Provides static properties to use in <em> select() and fetch() </em>").eol();
    writer.append("   * clauses of a query. Typically referenced via static imports. ").eol();
    writer.append("   */").eol();
    writer.append("  ").append(Constants.AT_GENERATED).eol();
    writer.append("  public static final class Alias {").eol();
    for (PropertyMeta property : properties) {
      property.writeFieldAliasDefn(writer, shortName);
      writer.eol();
    }
    writer.append("  }").eol();
  }

  private void writeAssocClass() {
    writer.eol();
    writer.append("  /**  Association query bean */").eol();
    writer.append("  ").append(Constants.AT_GENERATED).eol();
    writer.append("  ").append(Constants.AT_TYPEQUERYBEAN).eol();
    if (embeddable) {
      writer.append("  public static final class Assoc<R> extends TQAssoc<%s,R> {", shortInnerName).eol();
    } else {
      writer.append("  public static final class Assoc<R> extends TQAssocBean<%s,R,Q%s> {", shortName, shortInnerName).eol();
    }
    for (PropertyMeta property : properties) {
      writer.append("  ");
      property.writeFieldDefn(writer, shortName, true);
      writer.eol();
    }
    writer.eol();
    writeAssocBeanConstructor();
    if (!embeddable) {
      writeAssocFilterMany();
      writeAssocBeanFetch();
    }
    writer.append("  }").eol();
  }

  private void writeAssocFilterMany() {
    writer.eol();
    writer.append("    public final R filterMany(Consumer<Q%s> apply) {", shortName).eol();
    writer.append("      final ExpressionList list = Expr.factory().expressionList();", shortName).eol();
    writer.append("      final var qb = new Q%s(list);", shortName).eol();
    writer.append("      apply.accept(qb);").eol();
    writer.append("      expr().filterMany(_name).addAll(list);").eol();
    writer.append("      return _root;").eol();
    writer.append("    }").eol();
  }

  private void writeAssocBeanConstructor() {
    writer.append("    public Assoc(String name, R root) {").eol();
    writer.append("      super(name, root);").eol();
    writer.append("    }").eol().eol();

    writer.append("    public Assoc(String name, R root, String prefix) {").eol();
    writer.append("      super(name, root, prefix);").eol();
    writer.append("    }").eol();
  }

  private void writeAssocBeanFetch() {
    if (isEntity()) {
      // inherit the fetch methods
      if (implementsInterface != null) {
        writeAssocBeanExpression(false, "eq", "Is equal to by ID property.");
        writeAssocBeanExpression(true, "eqIfPresent", "Is equal to by ID property if the value is not null, if null no expression is added.");
        writeAssocBeanExpression(false, "in", "IN the given values.", implementsInterfaceShortName + "...", "in");
        writeAssocBeanExpression(false, "inBy", "IN the given interface values.", "Collection<? extends " + implementsInterfaceShortName + ">", "in");
        writeAssocBeanExpression(true, "inOrEmptyBy", "IN the given interface values if the collection is not empty. No expression is added if the collection is empty..", "Collection<? extends " + implementsInterfaceShortName + ">", "inOrEmpty");
      }
    }
  }

  private void writeAssocBeanExpression(boolean nullable,String expression, String comment) {
    writeAssocBeanExpression(nullable, expression, comment, implementsInterfaceShortName, expression);
  }

  private void writeAssocBeanExpression(boolean nullable, String expression, String comment, String param, String actualExpression) {
    final String nullableAnnotation = nullable ? "@Nullable " : "";
    String values = expression.startsWith("in") ? "values" : "value";
    String castVarargs = expression.equals("in") ? "(Object[])" : "";
    writer.append("  /**").eol();
    writer.append("   * ").append(comment).eol();
    writer.append("   */").eol();
    writer.append("  public final R %s(%s%s %s) {", expression, nullableAnnotation, param, values).eol();
    writer.append("    expr().%s(_name, %s%s);", actualExpression, castVarargs, values).eol();
    writer.append("    return _root;").eol();
    writer.append("  }").eol();
    writer.eol();
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  /**
   * Write all the imports.
   */
  private void writeImports() {
    for (String importType : importTypes) {
      writer.append("import %s;", importType).eol();
    }
    writer.eol();
  }

  private void writePackage() {
    writer.append("package %s;", destPackage).eol().eol();
  }

  private Writer createFileWriter() throws IOException {
    JavaFileObject jfo = processingContext.createWriter(destPackage + "." + "Q" + shortName, element);
    return jfo.openWriter();
  }

}
