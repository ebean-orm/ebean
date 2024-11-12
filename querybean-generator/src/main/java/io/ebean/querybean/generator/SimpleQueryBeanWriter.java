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
  private String implementsInterfaceShortName;
  private final ProcessingContext processingContext;
  private final String dbName;
  private final String beanFullName;
  private final boolean isEntity;
  private final boolean embeddable;

  private final String destPackage;
  private final String shortName;
  private final String shortInnerName;
  private final boolean fullyQualify;
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
    this.isEntity = processingContext.isEntity(element);
    this.embeddable = processingContext.isEmbeddable(element);
    this.dbName = findDbName();
    this.implementsInterface = initInterface(element);
    this.fullyQualify = processingContext.isNameClash(shortName);
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

  private void gatherPropertyDetails() {
    if (implementsInterface != null) {
      String implementsInterfaceFullName = implementsInterface.getQualifiedName().toString();
      boolean nested = implementsInterface.getNestingKind().isNested();
      implementsInterfaceShortName = Util.shortName(nested, implementsInterfaceFullName);

      importTypes.add(Constants.AVAJE_LANG_NULLABLE);
      importTypes.add(Constants.JAVA_COLLECTION);
      importTypes.add(implementsInterfaceFullName);
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
        type.addImports(importTypes, fullyQualify);
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
    writer.append("    return new Q%s(io.ebean.FetchGroup.queryFor(%s.class));", shortName, beanFullName).eol();
    writer.append("  }").eol();
    writer.eol();

    String name = (dbName == null) ? "default" : dbName;
    writer.append("  /** Construct using the %s Database */", name).eol();
    writer.append("  public Q%s() {", shortName).eol();
    if (dbName == null) {
      writer.append("    super(%s.class);", beanFullName).eol();
    } else {
      writer.append("    super(%s.class, io.ebean.DB.byName(\"%s\"));", beanFullName, dbName).eol();
    }
    writer.append("  }").eol();
    writer.eol();

    writer.eol();
    writer.append("  /** Construct with a given Database */").eol();
    writer.append("  public Q%s(io.ebean.Database database) {", shortName).eol();
    writer.append("    super(%s.class, database);", beanFullName).eol();
    writer.append("  }").eol();
    writer.eol();

    writer.eol();
    writer.append("  /** Private constructor for Alias */").eol();
    writer.append("  private Q%s(boolean dummy) {", shortName).eol();
    writer.append("    super(dummy);").eol();
    writer.append("  }").eol();

    writer.eol();
    writer.append("  /** Private constructor for FetchGroup building */").eol();
    writer.append("  private Q%s(io.ebean.Query<%s> fetchGroupQuery) {", shortName, beanFullName).eol();
    writer.append("    super(fetchGroupQuery);").eol();
    writer.append("  }").eol();

    writer.eol();
    writer.append("  /** Private constructor for filterMany */").eol();
    writer.append("  private Q%s(io.ebean.ExpressionList<%s> filter) {", shortName, beanFullName).eol();
    writer.append("    super(filter);").eol();
    writer.append("  }").eol();

    writer.eol();
    writer.append("  /** Return a copy of the query bean. */").eol();
    writer.append("  @Override").eol();
    writer.append("  public Q%s copy() {", shortName).eol();
    writer.append("    return new Q%s(query().copy());", shortName).eol();
    writer.append("  }").eol();
  }

  /**
   * Write all the fields.
   */
  private void writeFields() {
    for (PropertyMeta property : properties) {
      property.writeFieldDefn(writer, shortName, false, fullyQualify);
      writer.eol();
    }
    writer.eol();
  }

  private void writeClass() {
    writer.append("/**").eol();
    writer.append(" * Query bean for %s.", shortName).eol();
    writer.append(" * <p>").eol();
    writer.append(" * THIS IS A GENERATED OBJECT, DO NOT MODIFY THIS CLASS.").eol();
    writer.append(" */").eol();
    writer.append("@SuppressWarnings(\"unused\")").eol();
    writer.append(Constants.AT_GENERATED).eol();
    if (embeddable) {
      writer.append("public final class Q%s {", shortName).eol();
    } else {
      writer.append(Constants.AT_TYPEQUERYBEAN).eol();
      writer.append("public final class Q%s extends io.ebean.typequery.QueryBean<%s,Q%s> {", shortName, beanFullName, shortName).eol();
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
      property.writeFieldAliasDefn(writer, shortName, fullyQualify);
      writer.eol();
    }
    writer.append("  }").eol();
  }

  private void writeAssocClass() {
    writer.eol();
    writer.append("  /** Association query bean */").eol();
    writer.append("  ").append(Constants.AT_GENERATED).eol();
    writer.append("  ").append(Constants.AT_TYPEQUERYBEAN).eol();
    if (embeddable) {
      writer.append("  public static final class Assoc<R> extends io.ebean.typequery.TQAssoc<%s,R> {", beanFullName).eol();
    } else {
      writer.append("  public static abstract class Assoc<R> extends io.ebean.typequery.TQAssocBean<%s,R,Q%s> {", beanFullName, shortInnerName).eol();
    }
    writer.eol();
    for (PropertyMeta property : properties) {
      writer.append("  ");
      property.writeFieldDefn(writer, shortName, true, fullyQualify);
      writer.eol();
    }
    writer.eol();
    writeAssocBeanConstructor("protected Assoc");
    writeAssocBeanFetch();
    writer.append("  }").eol();
    if (!embeddable) {
      writeAssocOne();
      writeAssocMany();
    }
  }

  private void writeAssocOne() {
    writer.eol();
    writer.append("  /** Associated ToOne query bean */").eol();
    writer.append("  ").append(Constants.AT_GENERATED).eol();
    writer.append("  ").append(Constants.AT_TYPEQUERYBEAN).eol();
    writer.append("  public static final class AssocOne<R> extends Assoc<R> {").eol();
    writeAssocBeanConstructor("public AssocOne");
    writer.append("  }").eol();
  }

  private void writeAssocMany() {
    writer.eol();
    writer.append("  /** Associated ToMany query bean */").eol();
    writer.append("  ").append(Constants.AT_GENERATED).eol();
    writer.append("  ").append(Constants.AT_TYPEQUERYBEAN).eol();
    writer.append("  public static final class AssocMany<R> extends Assoc<R> implements io.ebean.typequery.TQAssocMany<%s, R, Q%s>{", beanFullName, shortInnerName).eol();
    writeAssocBeanConstructor("public AssocMany");
    writeAssocFilterMany();
    writer.append("  }").eol();
  }

  private void writeAssocFilterMany() {
    writer.eol();
    writer.append("    @Override").eol();
    writer.append("    public R filterMany(java.util.function.Consumer<Q%s> apply) {", shortName).eol();
    writer.append("      final io.ebean.ExpressionList<%s> list = _newExpressionList();", beanFullName).eol();
    writer.append("      apply.accept(new Q%s(list));", shortName).eol();
    writer.append("      return _filterMany(list);").eol();
    writer.append("    }").eol();
    writer.eol();
    writer.append("    @Override").eol();
    writer.append("    public R filterMany(io.ebean.ExpressionList<%s> filter) { return _filterMany(filter); }", beanFullName).eol();
    writer.eol();
    writer.append("    @Override").eol();
    writer.append("    public R filterManyRaw(String rawExpressions, Object... params) { return _filterManyRaw(rawExpressions, params); }").eol();
    writer.eol();
    writer.append("    @Override").eol();
    writer.append("    @Deprecated(forRemoval = true)").eol();
    writer.append("    public R filterMany(String expressions, Object... params) { return _filterMany(expressions, params); }").eol();
    writer.eol();
    writer.append("    @Override").eol();
    writer.append("    public R isEmpty() { return _isEmpty(); }").eol();
    writer.eol();
    writer.append("    @Override").eol();
    writer.append("    public R isNotEmpty() { return _isNotEmpty(); }").eol();

  }

  private void writeAssocBeanConstructor(String prefix) {
    writer.append("    %s(String name, R root) { super(name, root); }", prefix).eol();
    writer.append("    %s(String name, R root, String prefix) { super(name, root, prefix); }", prefix).eol();
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
