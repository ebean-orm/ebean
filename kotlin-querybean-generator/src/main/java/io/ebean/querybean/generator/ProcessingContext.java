package io.ebean.querybean.generator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Context for the source generation.
 */
class ProcessingContext implements Constants {

  private final ProcessingEnvironment processingEnv;
  private final String generatedSources;

  private final Types typeUtils;
  private final Filer filer;
  private final Messager messager;
  private final Elements elementUtils;

  private final PropertyTypeMap propertyTypeMap = new PropertyTypeMap();

  private final ReadModuleInfo readModuleInfo;

  /**
   * All entity packages regardless of DB (for META-INF/ebean-generated-info.mf).
   */
  private final Set<String> allEntityPackages = new TreeSet<>();

  private final Set<String> otherClasses = new TreeSet<>();

  /**
   * The DB name prefixed entities.
   */
  private final Set<String> prefixEntities = new TreeSet<>();

  /**
   * Entity classes for the default database.
   */
  private final Set<String> dbEntities = new TreeSet<>();

  /**
   * Entity classes for non default databases.
   */
  private final Map<String, Set<String>> otherDbEntities = new TreeMap<>();

  /**
   * All loaded entities regardless of db (to detect ones we add back from loadedPrefixEntities).
   */
  private final Set<String> loaded = new HashSet<>();

  /**
   * For partial compile the previous list of prefixed entity classes.
   */
  private List<String> loadedPrefixEntities = new ArrayList<>(); ;

  /**
   * The package for the generated EntityClassRegister.
   */
  private String factoryPackage;

  ProcessingContext(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
    this.typeUtils = processingEnv.getTypeUtils();
    this.filer = processingEnv.getFiler();
    this.messager = processingEnv.getMessager();
    this.elementUtils = processingEnv.getElementUtils();
    this.generatedSources = initGeneratedSources(processingEnv);
    this.readModuleInfo = new ReadModuleInfo(this);
  }

  TypeElement entityAnnotation() {
    return elementUtils.getTypeElement(ENTITY);
  }

  TypeElement embeddableAnnotation() {
    return elementUtils.getTypeElement(EMBEDDABLE);
  }

  TypeElement converterAnnotation() {
    return elementUtils.getTypeElement(CONVERTER);
  }

  TypeElement componentAnnotation() {
    return elementUtils.getTypeElement(EBEAN_COMPONENT);
  }

  private String initGeneratedSources(ProcessingEnvironment processingEnv) {
    String generatedDir = processingEnv.getOptions().get("kapt.kotlin.generated");
    return (generatedDir != null) ? generatedDir : "target/generated-sources/kapt/compile";
  }

  private boolean isTypeAvailable(String canonicalName) {
    return null != elementUtils.getTypeElement(canonicalName);
  }

  /**
   * Return the base directory to put the kotlin generated source.
   */
  String generatedSourcesDir() {
    return generatedSources;
  }

  /**
   * Escape the type (e.g. java.lang.String) from the TypeMirror toString().
   */
  private String typeDef(TypeMirror typeMirror) {
    if (typeMirror.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredType = (DeclaredType) typeMirror;
      return Split.trimType(declaredType.asElement().toString());
    } else {
      return Split.trimType(typeMirror.toString());
    }
  }

  private String trimAnnotations(String type) {
    int pos = type.indexOf("@");
    if (pos == -1) {
      return type;
    }
    String remainder = type.substring(0, pos) + type.substring(type.indexOf(' ') + 1);
    return trimAnnotations(remainder);
  }

  /**
   * Gather all the fields (properties) for the given bean element.
   */
  List<VariableElement> allFields(Element element) {
    List<VariableElement> list = new ArrayList<>();
    gatherProperties(list, element);
    return list;
  }

  /**
   * Recursively gather all the fields (properties) for the given bean element.
   */
  private void gatherProperties(List<VariableElement> fields, Element element) {
    TypeElement typeElement = (TypeElement) element;
    TypeMirror superclass = typeElement.getSuperclass();
    Element mappedSuper = typeUtils.asElement(superclass);
    if (isMappedSuperOrInheritance(mappedSuper)) {
      gatherProperties(fields, mappedSuper);
    }

    List<VariableElement> allFields = ElementFilter.fieldsIn(element.getEnclosedElements());
    for (VariableElement field : allFields) {
      if (!ignoreField(field)) {
        fields.add(field);
      }
    }
  }

  /**
   * Not interested in static, transient or Ebean internal fields.
   */
  private boolean ignoreField(VariableElement field) {
    return isStaticOrTransient(field) || ignoreEbeanInternalFields(field);
  }

  private boolean ignoreEbeanInternalFields(VariableElement field) {
    String fieldName = field.getSimpleName().toString();
    return fieldName.startsWith("_ebean") || fieldName.startsWith("_EBEAN");
  }

  private boolean isStaticOrTransient(VariableElement field) {
    Set<Modifier> modifiers = field.getModifiers();
    return (
      modifiers.contains(Modifier.STATIC) ||
      modifiers.contains(Modifier.TRANSIENT) ||
      hasAnnotations(field, "jakarta.persistence.Transient")
    );
  }

  private static boolean hasAnnotations(Element element, String... annotations) {
    return getAnnotation(element, annotations) != null;
  }

  private static AnnotationMirror getAnnotation(Element element, String... annotations) {
    if (element == null) {
      return null;
    }
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      final String name = annotationMirror.getAnnotationType().asElement().toString();
      for (String annotation : annotations) {
        if (annotation.equals(name)) {
          return annotationMirror;
        }
      }
    }
    return null;
  }

  private boolean isMappedSuperOrInheritance(Element mappedSuper) {
    return hasAnnotations(mappedSuper, MAPPED_SUPERCLASS, INHERITANCE, DISCRIMINATOR_VALUE);
  }

  private boolean isEntityOrEmbedded(Element mappedSuper) {
    return hasAnnotations(mappedSuper, ENTITY, EMBEDDABLE);
  }

  boolean isEntity(Element element) {
    return hasAnnotations(element, ENTITY);
  }

  boolean isEmbeddable(Element element) {
    return hasAnnotations(element, EMBEDDABLE);
  }

  private static boolean dbToMany(Element field) {
    return hasAnnotations(field, ONE_TO_MANY, MANY_TO_MANY);
  }

  /**
   * Find the DbName annotation and return name if found.
   */
  String findDbName(TypeElement element) {
    return FindDbName.value(element, typeUtils);
  }

  /**
   * Return true if it is a DbJson field.
   */
  private static boolean dbJsonField(Element field) {
    return hasAnnotations(field, DBJSON, DBJSONB);
  }

  /**
   * Return true if it is a DbArray field.
   */
  private static boolean dbArrayField(Element field) {
    return hasAnnotations(field, DBARRAY);
  }

  PropertyType getPropertyType(VariableElement field) {
    boolean toMany = dbToMany(field);
    if (dbJsonField(field)) {
      return propertyTypeMap.getDbJsonType();
    }
    if (dbArrayField(field)) {
      // get generic parameter type
      DeclaredType declaredType = (DeclaredType) field.asType();
      String fullType = typeDef(declaredType.getTypeArguments().get(0));
      String shortName = langShortType(Split.shortName(fullType));
      return new PropertyTypeArray(fullType, shortName);
    }
    final TypeMirror typeMirror = field.asType();
    TypeMirror currentType = typeMirror;
    while (currentType != null) {
      PropertyType type = propertyTypeMap.getType(typeDef(currentType));
      if (type != null) {
        // simple scalar type
        return type;
      }
      // go up in class hierarchy
      TypeElement fieldType = (TypeElement) typeUtils.asElement(currentType);
      currentType = (fieldType == null) ? null : fieldType.getSuperclass();
    }

    Element fieldType = typeUtils.asElement(typeMirror);
    if (fieldType == null) {
      return null;
    }
    // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=544288
    fieldType = elementUtils.getTypeElement(fieldType.toString());

    if (fieldType.getKind() == ElementKind.ENUM) {
      String fullType = typeDef(typeMirror);
      return new PropertyTypeEnum(fullType, Split.shortName(fullType));
    }

    // look for targetEntity annotation attribute
    final String targetEntity = readTargetEntity(field);
    if (targetEntity != null) {
      final TypeElement element = elementUtils.getTypeElement(targetEntity);
      if (isEntityOrEmbedded(element)) {
        boolean embeddable = isEmbeddable(element);
        return createPropertyTypeAssoc(embeddable, toMany, typeDef(element.asType()));
      }
    }
    if (isEntityOrEmbedded(fieldType)) {
      boolean embeddable = isEmbeddable(fieldType);
      return createPropertyTypeAssoc(embeddable, toMany, typeDef(typeMirror));
    }

    final PropertyType result;
    if (typeMirror.getKind() == TypeKind.DECLARED) {
      result = createManyTypeAssoc(field, (DeclaredType) typeMirror);
    } else {
      result = null;
    }

    if (result != null) {
      return result;
    } else {
      if (typeInstanceOf(typeMirror, "java.lang.Comparable")) {
        return new PropertyTypeScalarComparable(trimAnnotations(typeMirror.toString()));
      } else {
        return new PropertyTypeScalar(trimAnnotations(typeMirror.toString()));
      }
    }
  }

  Element asElement(TypeMirror argType) {
    if (argType.getKind() == TypeKind.WILDCARD) {
      argType = ((WildcardType) argType).getExtendsBound();
    }
    return typeUtils.asElement(argType);
  }

  private boolean typeInstanceOf(final TypeMirror typeMirror, final CharSequence desiredInterface) {
    TypeElement typeElement = (TypeElement) typeUtils.asElement(typeMirror);
    if (typeElement == null || typeElement.getQualifiedName().contentEquals("java.lang.Object")) {
      return false;
    }
    if (typeElement.getQualifiedName().contentEquals(desiredInterface)) {
      return true;
    }

    return typeInstanceOf(typeElement.getSuperclass(), desiredInterface) ||
      typeElement
        .getInterfaces()
        .stream()
        .anyMatch(t -> typeInstanceOf(t, desiredInterface));
  }

  private PropertyType createManyTypeAssoc(VariableElement field, DeclaredType declaredType) {
    boolean toMany = dbToMany(field);
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    if (typeArguments.size() == 1) {
      Element argElement = typeUtils.asElement(typeArguments.get(0));
      if (isEntityOrEmbedded(argElement)) {
        boolean embeddable = isEmbeddable(argElement);
        return createPropertyTypeAssoc(embeddable, toMany, typeDef(argElement.asType()));
      }
    } else if (typeArguments.size() == 2) {
      Element argElement = typeUtils.asElement(typeArguments.get(1));
      if (isEntityOrEmbedded(argElement)) {
        boolean embeddable = isEmbeddable(argElement);
        return createPropertyTypeAssoc(embeddable, toMany, typeDef(argElement.asType()));
      }
    }
    return null;
  }

  private String readTargetEntity(Element declaredType) {
    for (AnnotationMirror annotation : declaredType.getAnnotationMirrors()) {
      final Object targetEntity = readTargetEntityFromAnnotation(annotation);
      if (targetEntity != null) {
        return targetEntity.toString();
      }
    }
    return null;
  }

  private static Object readTargetEntityFromAnnotation(AnnotationMirror mirror) {
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
      if ("targetEntity".equals(entry.getKey().getSimpleName().toString())) {
        return entry.getValue().getValue();
      }
    }
    return null;
  }

  private String langShortType(String shortName) {
    if ("Integer".equals(shortName)) {
      return "Int";
    }
    return shortName;
  }

  /**
   * Create the QAssoc PropertyType.
   */
  private PropertyType createPropertyTypeAssoc(boolean embeddable, boolean toMany, String fullName) {
    TypeElement typeElement = elementUtils.getTypeElement(fullName);
    String type;
    if (typeElement.getNestingKind().isNested()) {
      type = typeElement.getEnclosingElement().toString() + "$" + typeElement.getSimpleName();
    } else {
      type = typeElement.getQualifiedName().toString();
    }

    String suffix = toMany ? "Many" : embeddable ? "": "One";
    String[] split = Split.split(type);
    String propertyName = "Q" + split[1] + ".Assoc" + suffix;
    String importName = split[0] + ".query.Q" + split[1];
    return new PropertyTypeAssoc(propertyName, importName);
  }

  /**
   * Prepend the package to the suffix taking null into account.
   */
  private String packageAppend(String origPackage) {
    if (origPackage == null) {
      return "query.assoc";
    } else {
      return origPackage + "." + "query.assoc";
    }
  }

  /**
   * Create a file writer for the given class name.
   */
  JavaFileObject createWriter(String factoryClassName) throws IOException {
    return filer.createSourceFile(factoryClassName);
  }

  void logError(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  void logWarn(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
  }

  void logNote(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
  }

  void readModuleInfo() {
    String factory = loadMetaInfServices();
    if (factory != null) {
      TypeElement factoryType = elementUtils.getTypeElement(factory);
      if (factoryType != null) {
        // previous prefixed entities to add back for partial compile
        final ModuleMeta read = readModuleInfo.read(factoryType);
        loadedPrefixEntities.addAll(read.getEntities());
        otherClasses.addAll(read.getOther());
      }
    }
  }

  /**
   * Register an entity with optional dbName.
   */
  void addEntity(String beanFullName, String dbName) {
    loaded.add(beanFullName);
    final String pkg = packageOf(beanFullName);
    if (pkg != null) {
      allEntityPackages.add(pkg);
      updateFactoryPackage(pkg);
    }
    if (dbName != null) {
      prefixEntities.add(dbName + ":" + beanFullName);
      otherDbEntities.computeIfAbsent(dbName, s -> new TreeSet<>()).add(beanFullName);
    } else {
      prefixEntities.add(beanFullName);
      dbEntities.add(beanFullName);
    }
  }

  /**
   * Add back entity classes for partial compile.
   */
  int complete() {
    int added = 0;
    for (String oldPrefixEntity : loadedPrefixEntities) {
      // maybe split as dbName:entityClass
      final String[] prefixEntityClass = oldPrefixEntity.split(":");

      String dbName = null;
      String entityClass;
      if (prefixEntityClass.length > 1) {
        dbName = prefixEntityClass[0];
        entityClass = prefixEntityClass[1];
      } else {
        entityClass = prefixEntityClass[0];
      }
      if (!loaded.contains(entityClass)) {
        addEntity(entityClass, dbName);
        added++;
      }
    }
    return added;
  }

  private String packageOf(String beanFullName) {
    final int pos = beanFullName.lastIndexOf('.');
    if (pos > -1) {
      return beanFullName.substring(0, pos);
    }
    return null;
  }

  private void updateFactoryPackage(String pkg) {
    if (pkg != null && (factoryPackage == null || factoryPackage.length() > pkg.length())) {
      factoryPackage = pkg;
    }
  }

  FileObject createMetaInfServicesWriter() throws IOException {
    return createMetaInfWriter(METAINF_SERVICES_MODULELOADER);
  }

  FileObject createManifestWriter() throws IOException {
    return createMetaInfWriter(Constants.METAINF_MANIFEST);
  }

  FileObject createMetaInfWriter(String target) throws IOException {
    return filer.createResource(StandardLocation.CLASS_OUTPUT, "", target);
  }

  boolean hasOtherClasses() {
    return !otherClasses.isEmpty();
  }

  Set<String> getOtherClasses() {
    return otherClasses;
  }

  void addOther(Element element) {
    otherClasses.add(element.toString());
  }

  Set<String> getPrefixEntities() {
    return prefixEntities;
  }

  Set<String> getDbEntities() {
    return dbEntities;
  }

  Map<String, Set<String>> getOtherDbEntities() {
    return otherDbEntities;
  }


  Set<String> getAllEntityPackages() {
    return allEntityPackages;
  }

  String getFactoryPackage() {
    return factoryPackage != null ? factoryPackage : "unknown";
  }

  /**
   * Return the class name of the generated EntityClassRegister
   * (such that we can read the current meta data for partial compile).
   */
  String loadMetaInfServices() {
    try {
      FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", METAINF_SERVICES_MODULELOADER);
      if (fileObject != null) {
        Reader reader = fileObject.openReader(true);
        LineNumberReader lineReader = new LineNumberReader(reader);
        String line = lineReader.readLine();
        if (line != null) {
          return line.trim();
        }
      }

    } catch (FileNotFoundException | NoSuchFileException e) {
      // ignore - no services file yet
    } catch (FilerException e) {
      logNote(null, "FilerException reading services file: " + e.getMessage());
    } catch (Exception e) {
      logError(null, "Error reading services file: " + e.getMessage());
    }
    return null;
  }
}
