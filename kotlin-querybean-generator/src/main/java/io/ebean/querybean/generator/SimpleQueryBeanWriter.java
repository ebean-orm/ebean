package io.ebean.querybean.generator;


import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A simple implementation that generates and writes query beans.
 */
class SimpleQueryBeanWriter {

  private static final String[] javaTypes = {
    "java.lang.String",
    "java.lang.Integer",
    "java.lang.Long",
    "java.lang.Double",
    "java.lang.Float",
    "java.lang.Short",
    "java.lang.Boolean",
    "java.lang.Byte",
    "java.lang.Char"
  };

  private static final String[] kotlinTypes = {
    "kotlin.String",
    "kotlin.Int",
    "kotlin.Long",
    "kotlin.Double",
    "kotlin.Float",
    "kotlin.Short",
    "kotlin.Boolean",
    "kotlin.Byte",
    "kotlin.Char"
  };

  // These are special classes under Kotlin, and are auto-imported, same as
  // java.lang under Java
  private static final Set<String> kotlinBlackListedImports = Collections.unmodifiableSet(
    new HashSet<>(
      Arrays.asList(
        "?",
        "extends",
        "java.util.ArrayList",
        "java.util.HashMap",
        "java.util.HashSet",
        "java.util.LinkedHashMap",
        "java.util.LinkedHashSet",
        "java.util.List",
        "java.util.Map",
        "java.util.Set"
      )
    )
  );

  private final Set<String> importTypes = new TreeSet<>();
  private final List<PropertyMeta> properties = new ArrayList<>();
  private final TypeElement element;
  private final TypeElement implementsInterface;
  private String implementsInterfaceFullName;
  private String implementsInterfaceShortName;
  private final ProcessingContext processingContext;
  private final boolean isEntity;
  private final boolean embeddable;
  private final String dbName;
  private final String beanFullName;
  private final KotlinLangAdapter langAdapter = new KotlinLangAdapter();
  private boolean writingEmbeddedBean;
  private final String generatedSourcesDir;

  private String destPackage;
  private String shortName;
  private final String shortInnerName;
  private final String origShortName;
  private Append writer;

  SimpleQueryBeanWriter(TypeElement element, ProcessingContext processingContext) {
    this.generatedSourcesDir = processingContext.generatedSourcesDir();
    this.element = element;
    this.processingContext = processingContext;
    this.beanFullName = element.getQualifiedName().toString();
    boolean nested = element.getNestingKind().isNested();
    this.destPackage = Util.packageOf(nested, beanFullName) + ".query";
    String sn = Util.shortName(nested, beanFullName);
    this.shortInnerName = Util.shortName(false, sn);
    this.shortName = sn.replace(".", "_"); // $ not supported with Kotlin, see kotlinInnerType()
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

  private static String kotlinInnerType(String fullType) {
    return fullType.replace('$', '_');
  }

  private void gatherPropertyDetails() {
    if (implementsInterface != null) {
      implementsInterfaceFullName = implementsInterface.getQualifiedName().toString();
      boolean nested = implementsInterface.getNestingKind().isNested();
      implementsInterfaceShortName = Util.shortName(nested, implementsInterfaceFullName);
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
   * Write the type query bean.
   */
  void writeBean() throws IOException {
    gatherPropertyDetails();
    translateKotlinImportTypes();
    if (isEmbeddable()) {
      processingContext.addEntity(beanFullName, dbName);
      writeEmbeddedBean();
    } else if (isEntity()) {
      processingContext.addEntity(beanFullName, dbName);
      writer = new Append(createFileWriter());

      writePackage();
      writeImports();
      writeClass();
      writeAlias();
      writeFields(false);
      writeConstructors();
      writeClassEnd();

      writer.close();
    }
  }

  /**
   * Translate the base types (String, Integer etc) to Kotlin types.
   */
  private void translateKotlinImportTypes() {
    for (int i = 0; i < javaTypes.length; i++) {
      if (importTypes.remove(javaTypes[i])) {
        importTypes.add(kotlinTypes[i]);
      }
    }
    importTypes.removeAll(kotlinBlackListedImports);
  }

  private boolean isEntity() {
    return isEntity;
  }

  private boolean isEmbeddable() {
    return embeddable;
  }

  /**
   * Write the type query assoc bean.
   */
  void writeEmbeddedBean() throws IOException {
    writingEmbeddedBean = true;

    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClass();
    writeEmbeddedAssoc();
    writeClassEnd();

    writer.close();
  }

  private void writeConstructors() {
    langAdapter.rootBeanConstructor(writer, shortName, dbName, beanFullName);
    writeAssocClasses();
  }

  private void writeFields(boolean assocBeans) {
    String padding = assocBeans ? "  " : "";
    for (PropertyMeta property : properties) {
      String typeDefn = toKotlinType(property.getTypeDefn(shortName, assocBeans));
      writer.append("%s  lateinit var %s: %s", padding, property.getName(), kotlinInnerType(typeDefn)).eol();
    }
    writer.eol();
  }

  private static String toKotlinType(String type)  {
    return type
      .replace("? extends Object", "Any")
      .replace(",Integer>", ",Int>");
  }

  /**
   * Write the class definition.
   */
  private void writeClass() {
    if (writingEmbeddedBean) {
      writer.append("/**").eol();
      writer.append(" * Association query bean for %s.", shortName).eol();
      writer.append(" * ").eol();
      writer.append(" * THIS IS A GENERATED OBJECT, DO NOT MODIFY THIS CLASS.").eol();
      writer.append(" */").eol();
      writer.append(Constants.AT_GENERATED).eol();
      writer.append("class Q%s {", shortName).eol();
    } else {
      writer.append("/**").eol();
      writer.append(" * Query bean for %s.", shortName).eol();
      writer.append(" * ").eol();
      writer.append(" * THIS IS A GENERATED OBJECT, DO NOT MODIFY THIS CLASS.").eol();
      writer.append(" */").eol();
      writer.append(Constants.AT_GENERATED).eol();
      writer.append(Constants.AT_TYPEQUERYBEAN).eol();
      writer.append("class Q%s : io.ebean.typequery.QueryBean<%s, Q%s> {", shortName, beanFullName, shortName).eol();
    }

    writer.eol();
  }

  private void writeAlias() {
    langAdapter.alias(writer, shortName, beanFullName);
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  /**
   * Write all the imports.
   */
  private void writeImports() {
    for (String importType : importTypes) {
      writer.append("import %s;", kotlinInnerType(importType)).eol();
    }
    writer.eol();
  }

  private void writePackage() {
    writer.append("package %s;", destPackage).eol().eol();
  }

  private Writer createFileWriter() throws IOException {
    String relPath = destPackage.replace('.', '/');
    File absDir = new File(generatedSourcesDir, relPath);
    if (!absDir.exists() && !absDir.mkdirs()) {
      processingContext.logNote("failed to create directories for:" + absDir.getAbsolutePath());
    }

    String fullPath = relPath + "/Q" + shortName + ".kt";
    File absFile = new File(generatedSourcesDir, fullPath);
    return new FileWriter(absFile);
  }

  private void writeEmbeddedAssoc() {
    writer.append("  @io.ebean.typequery.Generated(\"io.ebean.querybean.generator\") @io.ebean.typequery.TypeQueryBean(\"v1\")").eol();
    writer.append("  class Assoc<R> : io.ebean.typequery.TQAssoc<%s,R> {", beanFullName).eol().eol();
    writeFields(true);
    writer.append("    protected constructor(name: String, root: R) : super(name, root)").eol();
    writer.append("    protected constructor(name: String, root: R, prefix: String) : super(name, root, prefix)").eol();
    writer.append("  }").eol().eol();
  }

  private void writeAssocClasses() {
    writer.append("  @io.ebean.typequery.Generated(\"io.ebean.querybean.generator\") @io.ebean.typequery.TypeQueryBean(\"v1\")").eol();
    writer.append("  abstract class Assoc<R> : io.ebean.typequery.TQAssocBean<%s, R, Q%s> {", beanFullName, shortName).eol();

    writeFields(true);
    writer.append("    protected constructor(name: String, root: R) : super(name, root)").eol();
    writer.append("    protected constructor(name: String, root: R, prefix: String) : super(name, root, prefix)").eol();
    writer.append("  }").eol().eol();

    writer.append("  @io.ebean.typequery.Generated(\"io.ebean.querybean.generator\") @io.ebean.typequery.TypeQueryBean(\"v1\")").eol();
    writer.append("  class AssocOne<R> : Assoc<R> {").eol();
    writer.append("    constructor(name: String, root: R) : super(name, root)").eol();
    writer.append("    constructor(name: String, root: R, prefix: String) : super(name, root, prefix)").eol();
    writer.append("  }").eol().eol();

    writer.append("  @io.ebean.typequery.Generated(\"io.ebean.querybean.generator\") @io.ebean.typequery.TypeQueryBean(\"v1\")").eol();
    writer.append("  class AssocMany<R> : Assoc<R>, io.ebean.typequery.TQAssocMany<%s, R, Q%s> {", beanFullName, shortName).eol();
    writer.append("    constructor(name: String, root: R) : super(name, root)").eol();
    writer.append("    constructor(name: String, root: R, prefix: String) : super(name, root, prefix)").eol();
    writer.eol();
    writer.append("    override fun filterMany(apply: java.util.function.Consumer<Q%s>): R {", shortName).eol();
    writer.append("      val list: io.ebean.ExpressionList<%s> = _newExpressionList<%s>()", beanFullName, beanFullName).eol();
    writer.append("      apply.accept(Q%s(list))", shortName).eol();
    writer.append("      return _filterMany(list)").eol();
    writer.append("    }").eol().eol();
    writer.append("    override fun filterMany(filter: io.ebean.ExpressionList<%s>): R {", beanFullName).eol();
    writer.append("      return _filterMany(filter)").eol();
    writer.append("    }").eol().eol();
    writer.append("    override fun filterManyRaw(rawExpressions: String, vararg params: Any): R {").eol();
    writer.append("      return _filterManyRaw(rawExpressions, *params)").eol();
    writer.append("    }").eol().eol();

    writer.append("    override fun isEmpty(): R {").eol();
    writer.append("      return _isEmpty() ").eol();
    writer.append("    }").eol().eol();
    writer.append("    override fun isNotEmpty(): R {").eol();
    writer.append("      return _isNotEmpty() ").eol();
    writer.append("    }").eol().eol();

    writer.append("  }").eol();
  }
}
