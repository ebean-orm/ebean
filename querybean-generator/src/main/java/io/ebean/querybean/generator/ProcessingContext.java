package io.ebean.querybean.generator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Context for the source generation.
 */
class ProcessingContext implements Constants {

  private final ProcessingEnvironment processingEnv;
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
  private final Set<String> dbMappedSuper = new TreeSet<>();

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
  private final List<String> loadedPrefixEntities = new ArrayList<>();

  /**
   * The package for the generated EntityClassRegister.
   */
  private String factoryPackage;

  /**
   * Cache of resolved generic type parameters for mapped superclasses.
   */
  private final Map<String, Map<String, TypeMirror>> genericTypeCache = new HashMap<>();

  ProcessingContext(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
    this.typeUtils = processingEnv.getTypeUtils();
    this.filer = processingEnv.getFiler();
    this.messager = processingEnv.getMessager();
    this.elementUtils = processingEnv.getElementUtils();
    this.readModuleInfo = new ReadModuleInfo(this);
  }

  TypeElement entityAnnotation() {
    return elementUtils.getTypeElement(ENTITY);
  }

  TypeElement embeddableAnnotation() {
    return elementUtils.getTypeElement(EMBEDDABLE);
  }

  TypeElement mappedSuperclassAnnotation() {
    return elementUtils.getTypeElement(MAPPED_SUPERCLASS);
  }

  TypeElement converterAnnotation() {
    return elementUtils.getTypeElement(CONVERTER);
  }

  TypeElement componentAnnotation() {
    return elementUtils.getTypeElement(EBEAN_COMPONENT);
  }

  /**
   * Gather all the fields (properties) for the given bean element.
   */
  List<VariableElement> allFields(Element element) {
    List<VariableElement> list = new ArrayList<>();
    gatherProperties(list, element, null);
    return list;
  }

  /**
   * Recursively gather all the fields (properties) for the given bean element.
   */
  private void gatherProperties(List<VariableElement> fields, Element element, Map<String, TypeMirror> typeParameterMap) {
    TypeElement typeElement = (TypeElement) element;
    TypeMirror superclass = typeElement.getSuperclass();
    Element mappedSuper = typeUtils.asElement(superclass);
    if (isMappedSuperOrInheritance(mappedSuper)) {
      // Resolve generic type parameters for the superclass
      Map<String, TypeMirror> superTypeParameterMap = resolveGenericTypes(superclass, typeParameterMap);
      gatherProperties(fields, mappedSuper, superTypeParameterMap);
    }

    List<VariableElement> allFields = ElementFilter.fieldsIn(element.getEnclosedElements());
    for (VariableElement field : allFields) {
      if (!ignoreField(field)) {
        // Create a wrapper that holds both the field and its resolved type context
        if (typeParameterMap != null && !typeParameterMap.isEmpty()) {
          fields.add(new ResolvedVariableElement(field, typeParameterMap));
        } else {
          fields.add(field);
        }
      }
    }
  }

  /**
   * Wrapper class to hold a VariableElement along with its type parameter resolution context.
   * This allows us to pass resolved generic type information along with field elements.
   */
  private static class ResolvedVariableElement implements VariableElement {
    private final VariableElement delegate;
    private final Map<String, TypeMirror> typeParameterMap;

    ResolvedVariableElement(VariableElement delegate, Map<String, TypeMirror> typeParameterMap) {
      this.delegate = delegate;
      this.typeParameterMap = new HashMap<>(typeParameterMap);
    }

    public Map<String, TypeMirror> getTypeParameterMap() {
      return typeParameterMap;
    }

    // Delegate all VariableElement methods to the original element
    @Override public TypeMirror asType() { return delegate.asType(); }
    @Override public ElementKind getKind() { return delegate.getKind(); }
    @Override public Set<Modifier> getModifiers() { return delegate.getModifiers(); }
    @Override public javax.lang.model.element.Name getSimpleName() { return delegate.getSimpleName(); }
    @Override public Element getEnclosingElement() { return delegate.getEnclosingElement(); }
    @Override public List<? extends Element> getEnclosedElements() { return delegate.getEnclosedElements(); }
    @Override public List<? extends AnnotationMirror> getAnnotationMirrors() { return delegate.getAnnotationMirrors(); }
    @Override public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annotationType) { return delegate.getAnnotation(annotationType); }
    @Override public <A extends java.lang.annotation.Annotation> A[] getAnnotationsByType(Class<A> annotationType) { return delegate.getAnnotationsByType(annotationType); }
    @Override public Object getConstantValue() { return delegate.getConstantValue(); }
    @Override public <R, P> R accept(javax.lang.model.element.ElementVisitor<R, P> v, P p) { return delegate.accept(v, p); }
    @Override public String toString() { return delegate.toString(); }
    @Override public boolean equals(Object obj) { return delegate.equals(obj); }
    @Override public int hashCode() { return delegate.hashCode(); }
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

  private static boolean dbToMany(Element field) {
    return hasAnnotations(field, ONE_TO_MANY, MANY_TO_MANY);
  }

  /**
   * Escape the type (e.g. java.lang.String) from the TypeMirror toString().
   */
  private static String typeDef(TypeMirror typeMirror) {
    if (typeMirror.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredType = (DeclaredType) typeMirror;
      return declaredType.asElement().toString();
    } else {
      return typeMirror.toString();
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
   * Resolve generic type parameters for a superclass in the context of a subclass.
   * This method maps generic type parameters from the superclass to their actual types
   * as specified in the subclass declaration.
   *
   * @param superclassType The superclass type mirror (may be parameterized)
   * @param parentTypeParameterMap Existing type parameter mappings from parent context
   * @return A map of type parameter names to their resolved TypeMirror instances
   */
  private Map<String, TypeMirror> resolveGenericTypes(TypeMirror superclassType, Map<String, TypeMirror> parentTypeParameterMap) {
    Map<String, TypeMirror> typeParameterMap = new HashMap<>();

    // If we have parent type parameter mappings, inherit them
    if (parentTypeParameterMap != null) {
      typeParameterMap.putAll(parentTypeParameterMap);
    }

    if (superclassType.getKind() != TypeKind.DECLARED) {
      return typeParameterMap;
    }

    DeclaredType declaredSuperclass = (DeclaredType) superclassType;
    TypeElement superclassElement = (TypeElement) declaredSuperclass.asElement();

    // Get the type parameters from the superclass definition
    List<? extends TypeParameterElement> typeParameters = superclassElement.getTypeParameters();

    // Get the actual type arguments used in this specific inheritance
    List<? extends TypeMirror> typeArguments = declaredSuperclass.getTypeArguments();

    // Map each type parameter to its actual type
    for (int i = 0; i < typeParameters.size() && i < typeArguments.size(); i++) {
      String parameterName = typeParameters.get(i).getSimpleName().toString();
      TypeMirror actualType = typeArguments.get(i);

      // If the actual type is itself a type variable, try to resolve it from parent context
      if (actualType.getKind() == TypeKind.TYPEVAR && parentTypeParameterMap != null) {
        TypeVariable typeVar = (TypeVariable) actualType;
        String varName = typeVar.asElement().getSimpleName().toString();
        TypeMirror resolvedType = parentTypeParameterMap.get(varName);
        if (resolvedType != null) {
          actualType = resolvedType;
        }
      }

      typeParameterMap.put(parameterName, actualType);
    }

    // Cache the resolved types for performance
    String cacheKey = superclassElement.getQualifiedName().toString();
    genericTypeCache.put(cacheKey, new HashMap<>(typeParameterMap));

    return typeParameterMap;
  }

  /**
   * Resolve a field's type in the context of generic type parameters.
   * If the field type is a type variable, resolve it to the actual type.
   *
   * @param fieldType The original field type
   * @param typeParameterMap The resolved type parameter mappings
   * @return The resolved type mirror, or the original type if no resolution needed
   */
  private TypeMirror resolveFieldType(TypeMirror fieldType, Map<String, TypeMirror> typeParameterMap) {
    if (typeParameterMap == null || typeParameterMap.isEmpty()) {
      return fieldType;
    }

    if (fieldType.getKind() == TypeKind.TYPEVAR) {
      TypeVariable typeVar = (TypeVariable) fieldType;
      String parameterName = typeVar.asElement().getSimpleName().toString();
      TypeMirror resolvedType = typeParameterMap.get(parameterName);
      if (resolvedType != null) {
        return resolvedType;
      }
    }

    // Handle parameterized types (e.g., List<T> where T needs resolution)
    if (fieldType.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredType = (DeclaredType) fieldType;
      List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

      if (!typeArguments.isEmpty()) {
        List<TypeMirror> resolvedArguments = new ArrayList<>();
        boolean hasChanges = false;

        for (TypeMirror typeArg : typeArguments) {
          TypeMirror resolvedArg = resolveFieldType(typeArg, typeParameterMap);
          resolvedArguments.add(resolvedArg);
          if (resolvedArg != typeArg) {
            hasChanges = true;
          }
        }

        if (hasChanges) {
          // Create a new DeclaredType with resolved type arguments
          TypeElement typeElement = (TypeElement) declaredType.asElement();
          return typeUtils.getDeclaredType(typeElement, resolvedArguments.toArray(new TypeMirror[0]));
        }
      }
    }

    return fieldType;
  }

  PropertyType getPropertyType(VariableElement field) {
    // Check if this is a resolved field from a generic mapped superclass
    Map<String, TypeMirror> typeParameterMap = null;
    if (field instanceof ResolvedVariableElement) {
      typeParameterMap = ((ResolvedVariableElement) field).getTypeParameterMap();
    }

    boolean toMany = dbToMany(field);
    if (dbJsonField(field)) {
      return propertyTypeMap.getDbJsonType();
    }
    if (dbArrayField(field)) {
      // get generic parameter type
      DeclaredType declaredType = (DeclaredType) field.asType();
      TypeMirror arrayElementType = declaredType.getTypeArguments().get(0);
      // Resolve the array element type if it's generic
      TypeMirror resolvedElementType = resolveFieldType(arrayElementType, typeParameterMap);
      String fullType = typeDef(resolvedElementType);
      return new PropertyTypeArray(fullType, Split.shortName(fullType));
    }

    // Get the field type, potentially resolved if it's generic
    final TypeMirror originalTypeMirror = field.asType();
    final TypeMirror typeMirror = resolveFieldType(originalTypeMirror, typeParameterMap);

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
    if (fieldType != null && fieldType.getKind() == ElementKind.ENUM) {
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
      //  public QAssocContact<QCustomer> contacts;
      boolean embeddable = isEmbeddable(fieldType);
      return createPropertyTypeAssoc(embeddable, toMany, typeDef(typeMirror));
    }

    final PropertyType result;
    if (typeMirror.getKind() == TypeKind.DECLARED) {
      result = createManyTypeAssoc(field, (DeclaredType) typeMirror, typeParameterMap);
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

  private PropertyType createManyTypeAssoc(VariableElement field, DeclaredType declaredType, Map<String, TypeMirror> typeParameterMap) {
    boolean toMany = dbToMany(field);
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    if (typeArguments.size() == 1) {
      TypeMirror argType = typeArguments.get(0);
      // Resolve the type argument if it's generic
      TypeMirror resolvedArgType = resolveFieldType(argType, typeParameterMap);
      Element argElement = typeUtils.asElement(resolvedArgType);
      if (isEntityOrEmbedded(argElement)) {
        boolean embeddable = isEmbeddable(argElement);
        return createPropertyTypeAssoc(embeddable, toMany, typeDef(resolvedArgType));
      }
    } else if (typeArguments.size() == 2) {
      TypeMirror argType = typeArguments.get(1);
      // Resolve the type argument if it's generic
      TypeMirror resolvedArgType = resolveFieldType(argType, typeParameterMap);
      Element argElement = typeUtils.asElement(resolvedArgType);
      if (isEntityOrEmbedded(argElement)) {
        boolean embeddable = isEmbeddable(argElement);
        return createPropertyTypeAssoc(embeddable, toMany, typeDef(resolvedArgType));
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
   * Create a file writer for the given class name.
   */
  JavaFileObject createWriter(String factoryClassName, Element originatingElement) throws IOException {
    return filer.createSourceFile(factoryClassName, originatingElement);
  }

  /**
   * Create a file writer for the given class name without an originating element.
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
    return createMetaInfWriter(METAINF_MANIFEST);
  }

  FileObject createNativeImageWriter(String name) throws IOException {
    String nm = "META-INF/native-image/" + name + "/reflect-config.json";
    return createMetaInfWriter(nm);
  }

  FileObject createMetaInfWriter(String target) throws IOException {
    return filer.createResource(StandardLocation.CLASS_OUTPUT, "", target);
  }

  public boolean hasOtherClasses() {
    return !otherClasses.isEmpty();
  }

  public Set<String> getOtherClasses() {
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
   * (such that we can read the current metadata for partial compile).
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

  Element asElement(TypeMirror mirror) {
    return typeUtils.asElement(mirror);
  }

  boolean isNameClash(String shortName) {
    return propertyTypeMap.isNameClash(shortName);
  }

  void addMappedSuper(String fullName) {
    dbMappedSuper.add(fullName);
  }

  Set<String> getMappedSuper() {
    return dbMappedSuper;
  }
}
