package io.ebean.querybean.generator;


import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
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

  private final ProcessingContext processingContext;

  private final String dbName;
  private final String beanFullName;
  private final boolean isEntity;
  private final boolean embeddable;
  private boolean writingAssocBean;

  private String destPackage;
  private String origDestPackage;

  private String shortName;
  private String origShortName;
  private Append writer;

  SimpleQueryBeanWriter(TypeElement element, ProcessingContext processingContext) {
    this.element = element;
    this.processingContext = processingContext;
    this.beanFullName = element.getQualifiedName().toString();
    this.destPackage = derivePackage(beanFullName) + ".query";
    this.shortName = deriveShortName(beanFullName);
    this.isEntity = processingContext.isEntity(element);
    this.embeddable = processingContext.isEmbeddable(element);
    this.dbName = findDbName();
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
    final String generated = processingContext.getGeneratedAnnotation();
    if (generated != null) {
      importTypes.add(generated);
    }
    importTypes.add(beanFullName);
    importTypes.add(Constants.TQROOTBEAN);
    importTypes.add(Constants.TYPEQUERYBEAN);
    importTypes.add(Constants.DATABASE);
    importTypes.add(Constants.FETCHGROUP);
    importTypes.add(Constants.QUERY);
    importTypes.add(Constants.TRANSACTION);

    if (dbName != null) {
      importTypes.add(Constants.DB);
    }
    addClassProperties();
  }

  /**
   * Recursively add properties from the inheritance hierarchy.
   * <p>
   * Includes properties from mapped super classes and usual inheritance.
   * </p>
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
    if (isEmbeddable()) {
      processingContext.addEntity(beanFullName, dbName);
    } else if (isEntity()) {
      processingContext.addEntity(beanFullName, dbName);
      writer = new Append(createFileWriter());

      writePackage();
      writeImports();
      writeClass();
      writeAlias();
      writeFields();
      writeConstructors();
      writeStaticAliasClass();
      writeClassEnd();

      writer.close();
    }
  }

  /**
   * Write the type query assoc bean.
   */
  void writeAssocBean() throws IOException {
    writingAssocBean = true;
    origDestPackage = destPackage;
    destPackage = destPackage + ".assoc";
    origShortName = shortName;
    shortName = "Assoc" + shortName;

    prepareAssocBeanImports();

    writer = new Append(createFileWriter());

    writePackage();
    writeImports();
    writeClass();
    writeFields();
    writeConstructors();
    writeClassEnd();

    writer.close();
  }

  /**
   * Prepare the imports for writing assoc bean.
   */
  private void prepareAssocBeanImports() {
    importTypes.remove(Constants.DB);
    importTypes.remove(Constants.TQROOTBEAN);
    importTypes.remove(Constants.DATABASE);
    importTypes.remove(Constants.FETCHGROUP);
    importTypes.remove(Constants.QUERY);
    importTypes.add(Constants.TQASSOCBEAN);
    if (isEntity()) {
      importTypes.add(Constants.TQPROPERTY);
      importTypes.add(origDestPackage + ".Q" + origShortName);
    }

    // remove imports for the same package
    Iterator<String> importsIterator = importTypes.iterator();
    String checkImportStart = destPackage + ".QAssoc";
    while (importsIterator.hasNext()) {
      String importType = importsIterator.next();
      if (importType.startsWith(checkImportStart)) {
        importsIterator.remove();
      }
    }
  }

  /**
   * Write constructors.
   */
  private void writeConstructors() {
    if (writingAssocBean) {
      writeAssocBeanFetch();
      writeAssocBeanConstructor();
    } else {
      writeRootBeanConstructor();
    }
  }

  /**
   * Write the constructors for 'root' type query bean.
   */
  private void writeRootBeanConstructor() {

    writer.eol();
    writer.append("  /**").eol();
    writer.append("   * Return a query bean used to build a FetchGroup.").eol();
    writer.append("   */").eol();
    writer.append("  public static Q%s forFetchGroup() {", shortName).eol();
    writer.append("    return new Q%s(FetchGroup.queryFor(%s.class));", shortName, shortName).eol();
    writer.append("  }").eol();
    writer.eol();

    String name = (dbName == null) ? "default" : dbName;
    writer.append("  /**").eol();
    writer.append("   * Construct using the %s Database.", name).eol();
    writer.append("   */").eol();
    writer.append("  public Q%s() {", shortName).eol();
    if (dbName == null) {
      writer.append("    super(%s.class);", shortName).eol();
    } else {
      writer.append("    super(%s.class, DB.byName(\"%s\"));", shortName, dbName).eol();
    }
    writer.append("  }").eol();
    writer.eol();

    writer.append("  /**").eol();
    writer.append("   * Construct with a given transaction.", name).eol();
    writer.append("   */").eol();
    writer.append("  public Q%s(Transaction transaction) {", shortName).eol();
    if (dbName == null) {
      writer.append("    super(%s.class, transaction);", shortName).eol();
    } else {
      writer.append("    super(%s.class, DB.byName(\"%s\"), transaction);", shortName, dbName).eol();
    }
    writer.append("  }").eol();

    writer.eol();
    writer.append("  /**").eol();
    writer.append("   * Construct with a given Database.").eol();
    writer.append("   */").eol();
    writer.append("  public Q%s(Database database) {", shortName).eol();
    writer.append("    super(%s.class, database);", shortName).eol();
    writer.append("  }").eol();
    writer.eol();

    writer.eol();
    writer.append("  /**").eol();
    writer.append("   * Construct for Alias.").eol();
    writer.append("   */").eol();
    writer.append("  private Q%s(boolean dummy) {", shortName).eol();
    writer.append("    super(dummy);").eol();
    writer.append("  }").eol();

    writer.eol();
    writer.append("  /**").eol();
    writer.append("   * Private constructor for FetchGroup building.").eol();
    writer.append("   */").eol();
    writer.append("  private Q%s(Query<%s> fetchGroupQuery) {", shortName, shortName).eol();
    writer.append("    super(fetchGroupQuery);").eol();
    writer.append("  }").eol();
  }

  private void writeAssocBeanFetch() {
    if (isEntity()) {
      writeAssocBeanFetch("", "Eagerly fetch this association loading the specified properties.");
      writeAssocBeanFetch("Query", "Eagerly fetch this association using a 'query join' loading the specified properties.");
      writeAssocBeanFetch("Cache", "Eagerly fetch this association using L2 cache.");
      writeAssocBeanFetch("Lazy", "Use lazy loading for this association loading the specified properties.");
    }
  }

  private void writeAssocBeanFetch(String fetchType, String comment) {
    writer.append("  /**").eol();
    writer.append("   * ").append(comment).eol();
    writer.append("   */").eol();
    writer.append("  @SafeVarargs @SuppressWarnings(\"varargs\")").eol();
    writer.append("  public final R fetch%s(TQProperty<Q%s>... properties) {", fetchType, origShortName).eol();
    writer.append("    return fetch%sProperties(properties);", fetchType).eol();
    writer.append("  }").eol();
    writer.eol();
  }

  /**
   * Write constructor for 'assoc' type query bean.
   */
  private void writeAssocBeanConstructor() {
    writer.append("  public Q%s(String name, R root) {", shortName).eol();
    writer.append("    super(name, root);").eol();
    writer.append("  }").eol().eol();

    writer.append("  public Q%s(String name, R root, String prefix) {", shortName).eol();
    writer.append("    super(name, root, prefix);").eol();
    writer.append("  }").eol();
  }

  /**
   * Write all the fields.
   */
  private void writeFields() {
    for (PropertyMeta property : properties) {
      property.writeFieldDefn(writer, shortName, writingAssocBean);
      writer.eol();
    }
    writer.eol();
  }

  /**
   * Write the class definition.
   */
  private void writeClass() {
    if (writingAssocBean) {
      writer.append("/**").eol();
      writer.append(" * Association query bean for %s.", shortName).eol();
      writer.append(" * ").eol();
      writer.append(" * THIS IS A GENERATED OBJECT, DO NOT MODIFY THIS CLASS.").eol();
      writer.append(" */").eol();
      if (processingContext.isGeneratedAvailable()) {
        writer.append(Constants.AT_GENERATED).eol();
      }
      writer.append(Constants.AT_TYPEQUERYBEAN).eol();
      writer.append("public class Q%s<R> extends TQAssocBean<%s,R> {", shortName, origShortName).eol();

    } else {
      writer.append("/**").eol();
      writer.append(" * Query bean for %s.", shortName).eol();
      writer.append(" * ").eol();
      writer.append(" * THIS IS A GENERATED OBJECT, DO NOT MODIFY THIS CLASS.").eol();
      writer.append(" */").eol();
      if (processingContext.isGeneratedAvailable()) {
        writer.append(Constants.AT_GENERATED).eol();
      }
      writer.append(Constants.AT_TYPEQUERYBEAN).eol();
      writer.append("public class Q%s extends TQRootBean<%1$s,Q%1$s> {", shortName).eol();
    }

    writer.eol();
  }

  private void writeAlias() {
    if (!writingAssocBean) {
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
  }

  private void writeStaticAliasClass() {
    writer.eol();
    writer.append("  /**").eol();
    writer.append("   * Provides static properties to use in <em> select() and fetch() </em>").eol();
    writer.append("   * clauses of a query. Typically referenced via static imports. ").eol();
    writer.append("   */").eol();
    writer.append("  public static class Alias {").eol();
    for (PropertyMeta property : properties) {
      property.writeFieldAliasDefn(writer, shortName);
      writer.eol();
    }
    writer.append("  }").eol();
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

  private String derivePackage(String name) {
    int pos = name.lastIndexOf('.');
    if (pos == -1) {
      return "";
    }
    return name.substring(0, pos);
  }

  private String deriveShortName(String name) {
    int pos = name.lastIndexOf('.');
    if (pos == -1) {
      return name;
    }
    return name.substring(pos + 1);
  }
}
