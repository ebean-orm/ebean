package io.ebean.querybean.generator;

import static io.ebean.querybean.generator.APContext.filer;
import static io.ebean.querybean.generator.APContext.logError;
import static io.ebean.querybean.generator.APContext.logNote;
import static io.ebean.querybean.generator.APContext.typeElement;
import static io.ebean.querybean.generator.ProcessorUtils.trimAnnotations;

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

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
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
import javax.lang.model.util.ElementFilter;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/** Context for the source generation. */
class ProcessingContext implements Constants {

  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();

  private static final class Ctx {
    private final Set<String> services = new TreeSet<>();

    private final PropertyTypeMap propertyTypeMap = new PropertyTypeMap();

    /** All entity packages regardless of DB (for META-INF/ebean-generated-info.mf). */
    private final Set<String> allEntityPackages = new TreeSet<>();

    private final Set<String> otherClasses = new TreeSet<>();

    /** The DB name prefixed entities. */
    private final Set<String> prefixEntities = new TreeSet<>();

    /** Entity classes for the default database. */
    private final Set<String> dbEntities = new TreeSet<>();

    /** Entity classes for non default databases. */
    private final Map<String, Set<String>> otherDbEntities = new TreeMap<>();

    /**
     * All loaded entities regardless of db (to detect ones we add back from loadedPrefixEntities).
     */
    private final Set<String> loaded = new HashSet<>();

    /** For partial compile the previous list of prefixed entity classes. */
    private final List<String> loadedPrefixEntities = new ArrayList<>();

    /** The package for the generated EntityClassRegister. */
    private String factoryPackage;
  }

  static void init(ProcessingEnvironment processingEnv) {
    APContext.init(processingEnv);
    CTX.set(new Ctx());
  }

  static void clear() {
    APContext.clear();

    CTX.remove();
  }

  /** Gather all the fields (properties) for the given bean element. */
  static List<VariableElement> allFields(Element element) {
    List<VariableElement> list = new ArrayList<>();
    gatherProperties(list, element);
    return list;
  }

  /** Recursively gather all the fields (properties) for the given bean element. */
  private static void gatherProperties(List<VariableElement> fields, Element element) {
    TypeElement typeElement = (TypeElement) element;
    TypeMirror superclass = typeElement.getSuperclass();
    var mappedSuper = APContext.asTypeElement(superclass);
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

  /** Not interested in static, transient or Ebean internal fields. */
  private static boolean ignoreField(VariableElement field) {
    return isStaticOrTransient(field) || ignoreEbeanInternalFields(field);
  }

  private static boolean ignoreEbeanInternalFields(VariableElement field) {
    String fieldName = field.getSimpleName().toString();
    return fieldName.startsWith("_ebean") || fieldName.startsWith("_EBEAN");
  }

  private static boolean isStaticOrTransient(VariableElement field) {
    Set<Modifier> modifiers = field.getModifiers();
    return (modifiers.contains(Modifier.STATIC)
        || modifiers.contains(Modifier.TRANSIENT)
        || hasAnnotations(field, "jakarta.persistence.Transient"));
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

  private static boolean isMappedSuperOrInheritance(Element mappedSuper) {
    return hasAnnotations(mappedSuper, MAPPED_SUPERCLASS, INHERITANCE, DISCRIMINATOR_VALUE);
  }

  private static boolean isEntityOrEmbedded(Element mappedSuper) {

    return EntityPrism.isPresent(mappedSuper) || EmbeddablePrism.isPresent(mappedSuper);
  }

  static boolean isEmbeddable(Element element) {
    return EmbeddablePrism.isPresent(element);
  }

  /** Find the DbName annotation and return name if found. */
  static String findDbName(TypeElement element) {

    return DbNamePrism.getOptionalOn(element).map(DbNamePrism::value).orElse(null);
  }

  /** Return true if it is a DbJson field. */
  private static boolean dbJsonField(Element field) {
    return DbJsonPrism.isPresent(field) || DbJsonBPrism.isPresent(field);
  }

  private static boolean dbToMany(Element field) {
    return OneToManyPrism.isPresent(field) || ManyToManyPrism.isPresent(field);
  }

  /** Escape the type (e.g. java.lang.String) from the TypeMirror toString(). */
  private static String typeDef(TypeMirror typeMirror) {
    if (typeMirror.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredType = (DeclaredType) typeMirror;
      return declaredType.asElement().toString();
    } else {
      return typeMirror.toString();
    }
  }

  static PropertyType getPropertyType(VariableElement field) {
    boolean toMany = dbToMany(field);
    if (dbJsonField(field)) {
      return CTX.get().propertyTypeMap.getDbJsonType();
    }
    if (DbArrayPrism.isPresent(field)) {
      // get generic parameter type
      DeclaredType declaredType = (DeclaredType) field.asType();
      String fullType = typeDef(declaredType.getTypeArguments().get(0));
      return new PropertyTypeArray(fullType, Split.shortName(fullType));
    }
    final TypeMirror typeMirror = field.asType();
    TypeMirror currentType = typeMirror;
    while (currentType != null) {
      PropertyType type = CTX.get().propertyTypeMap.getType(typeDef(currentType));
      if (type != null) {
        // simple scalar type
        return type;
      }
      // go up in class hierarchy
      TypeElement fieldType = APContext.asTypeElement(currentType);
      currentType = (fieldType == null) ? null : fieldType.getSuperclass();
    }

    Element fieldType = APContext.asTypeElement(typeMirror);
    if (fieldType == null) {
      return null;
    }

    // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=544288
    fieldType = typeElement(fieldType.toString());
    if (fieldType.getKind() == ElementKind.ENUM) {
      String fullType = typeDef(typeMirror);
      return new PropertyTypeEnum(fullType, Split.shortName(fullType));
    }

    // look for targetEntity annotation attribute
    final String targetEntity = readTargetEntity(field);
    if (targetEntity != null) {
      final TypeElement element = typeElement(targetEntity);
      if (isEntityOrEmbedded(element)) {
        boolean embeddable = isEmbeddable(element);
        return createPropertyTypeAssoc(embeddable, toMany, typeDef(element.asType()));
      }
    }

    if (isEntityOrEmbedded(fieldType)) {
      //  public QAssocContact<QCustomer> contacts;
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
    } else if (APContext.isAssignable(typeMirror.toString(), "java.lang.Comparable")) {
      return new PropertyTypeScalarComparable(trimAnnotations(typeMirror.toString()));
    } else {
      return new PropertyTypeScalar(trimAnnotations(typeMirror.toString()));
    }
  }

  private static PropertyType createManyTypeAssoc(
      VariableElement field, DeclaredType declaredType) {
    boolean toMany = dbToMany(field);
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    if (typeArguments.size() == 1) {
      Element argElement = APContext.asTypeElement(typeArguments.get(0));
      if (isEntityOrEmbedded(argElement)) {
        boolean embeddable = isEmbeddable(argElement);
        return createPropertyTypeAssoc(embeddable, toMany, typeDef(argElement.asType()));
      }
    } else if (typeArguments.size() == 2) {
      Element argElement = APContext.asTypeElement(typeArguments.get(1));
      if (isEntityOrEmbedded(argElement)) {
        boolean embeddable = isEmbeddable(argElement);
        return createPropertyTypeAssoc(embeddable, toMany, typeDef(argElement.asType()));
      }
    }
    return null;
  }

  private static String readTargetEntity(Element declaredType) {
    for (AnnotationMirror annotation : declaredType.getAnnotationMirrors()) {
      final Object targetEntity = readTargetEntityFromAnnotation(annotation);
      if (targetEntity != null) {
        return targetEntity.toString();
      }
    }
    return null;
  }

  private static Object readTargetEntityFromAnnotation(AnnotationMirror mirror) {
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        mirror.getElementValues().entrySet()) {
      if ("targetEntity".equals(entry.getKey().getSimpleName().toString())) {
        return entry.getValue().getValue();
      }
    }
    return null;
  }

  /** Create the QAssoc PropertyType. */
  private static PropertyType createPropertyTypeAssoc(
      boolean embeddable, boolean toMany, String fullName) {
    TypeElement typeElement = typeElement(fullName);
    String type;
    if (typeElement.getNestingKind().isNested()) {
      type = typeElement.getEnclosingElement().toString() + "$" + typeElement.getSimpleName();
    } else {
      type = typeElement.getQualifiedName().toString();
    }

    String suffix = toMany ? "Many" : embeddable ? "" : "One";
    String[] split = Split.split(type);
    String propertyName = "Q" + split[1] + ".Assoc" + suffix;
    String importName = split[0] + ".query.Q" + split[1];
    return new PropertyTypeAssoc(propertyName, importName);
  }

  static void readModuleInfo() {
    String factory = loadMetaInfServices();
    if (factory != null) {
      TypeElement factoryType = APContext.typeElement(factory);
      if (factoryType != null) {

        // previous prefixed entities to add back for partial compile
        var p = ModuleInfoPrism.getInstanceOn(factoryType);
        CTX.get().loadedPrefixEntities.addAll(p.entities());
        CTX.get().otherClasses.addAll(p.other());
      }
    }
  }

  /** Register an entity with optional dbName. */
  static void addEntity(String beanFullName, String dbName) {
    CTX.get().loaded.add(beanFullName);
    final String pkg = packageOf(beanFullName);
    if (pkg != null) {
      CTX.get().allEntityPackages.add(pkg);
      updateFactoryPackage(pkg);
    }
    if (dbName != null) {
      CTX.get().prefixEntities.add(dbName + ":" + beanFullName);
      CTX.get().otherDbEntities.computeIfAbsent(dbName, s -> new TreeSet<>()).add(beanFullName);
    } else {
      CTX.get().prefixEntities.add(beanFullName);
      CTX.get().dbEntities.add(beanFullName);
    }
  }

  /** Add back entity classes for partial compile. */
  static int complete() {
    int added = 0;
    for (String oldPrefixEntity : CTX.get().loadedPrefixEntities) {
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
      if (!CTX.get().loaded.contains(entityClass)) {
        addEntity(entityClass, dbName);
        added++;
      }
    }
    return added;
  }

  private static String packageOf(String beanFullName) {
    final int pos = beanFullName.lastIndexOf('.');
    if (pos > -1) {
      return beanFullName.substring(0, pos);
    }
    return null;
  }

  private static void updateFactoryPackage(String pkg) {
    if (pkg != null
        && (CTX.get().factoryPackage == null || CTX.get().factoryPackage.length() > pkg.length())) {
      CTX.get().factoryPackage = pkg;
    }
  }

  static FileObject createMetaInfServicesWriter() throws IOException {
    return createMetaInfWriter(METAINF_SERVICES_MODULELOADER);
  }

  static FileObject createManifestWriter() throws IOException {
    return createMetaInfWriter(METAINF_MANIFEST);
  }

  static FileObject createNativeImageWriter(String name) throws IOException {
    String nm = "META-INF/native-image/" + name + "/reflect-config.json";
    return createMetaInfWriter(nm);
  }

  static FileObject createMetaInfWriter(String target) throws IOException {
    return filer().createResource(StandardLocation.CLASS_OUTPUT, "", target);
  }

  static boolean hasOtherClasses() {
    return !CTX.get().otherClasses.isEmpty();
  }

  static Set<String> getOtherClasses() {
    return CTX.get().otherClasses;
  }

  static void addOther(Element element) {
    CTX.get().otherClasses.add(element.toString());
  }

  static Set<String> getPrefixEntities() {
    return CTX.get().prefixEntities;
  }

  static Set<String> getDbEntities() {
    return CTX.get().dbEntities;
  }

  static Map<String, Set<String>> getOtherDbEntities() {
    return CTX.get().otherDbEntities;
  }

  static Set<String> getAllEntityPackages() {
    return CTX.get().allEntityPackages;
  }

  static String getFactoryPackage() {
    return CTX.get().factoryPackage != null ? CTX.get().factoryPackage : "unknown";
  }

  /**
   * Return the class name of the generated EntityClassRegister (such that we can read the current
   * metadata for partial compile).
   */
  static String loadMetaInfServices() {
    try {
      FileObject fileObject =
          filer().getResource(StandardLocation.CLASS_OUTPUT, "", METAINF_SERVICES_MODULELOADER);
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
      logNote("FilerException reading services file: " + e.getMessage());
    } catch (Exception e) {
      logError("Error reading services file: " + e.getMessage());
    }
    return null;
  }

  static boolean isNameClash(String shortName) {
    return CTX.get().propertyTypeMap.isNameClash(shortName);
  }

  static void validateModule() {
    APContext.moduleInfoReader()
        .ifPresent(r -> r.validateServices(AT_GENERATED, CTX.get().services));
  }

  public static void addService(String factoryFullName) {
    CTX.get().services.add(factoryFullName);
  }
}
