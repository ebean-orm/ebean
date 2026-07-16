package io.ebean.querybean.generator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Process compiled entity beans and generates 'query beans' for them.
 */
public class Processor extends AbstractProcessor implements Constants {

  private ProcessingContext processingContext;
  private DtoMappingReader dtoMappingReader;
  private boolean wroteDtoMappers;

  private boolean wroteLookup;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.processingContext = new ProcessingContext(processingEnv);
    this.dtoMappingReader = new DtoMappingReader(processingContext);
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotations = new LinkedHashSet<>();
    annotations.add(ENTITY);
    annotations.add(EMBEDDABLE);
    annotations.add(CONVERTER);
    annotations.add(EBEAN_COMPONENT);
    annotations.add(MODULEINFO);
    annotations.add(TYPEQUERYBEAN);
    annotations.add(GENERATED);
    annotations.add(DTO_MAPPING);
    annotations.add(DTO_MAPPING_LIST);
    annotations.add(DTO_MIXIN);
    annotations.add(DTO_CONVERTERS);
    return annotations;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    processingContext.readModuleInfo();
    int count = processEntities(roundEnv);
    processOthers(roundEnv);
    final int loaded = processingContext.complete();
    dtoMappingReader.collect(roundEnv);
    if (!roundEnv.processingOver()) {
      writeDtoMappers();
    }
    if (roundEnv.processingOver()) {
      writeModuleInfoBean();
    }
    if (count > 0) {
      String msg = "Ebean APT generated %s query beans, loaded %s others - META-INF/ebean-generated-info.mf entity-packages: %s";
      processingContext.logNote(msg, count, loaded, processingContext.getAllEntityPackages());
    }
    if (!wroteLookup) {
      wroteLookup = true;
      LookupWriter.write(processingContext, processingEnv.getElementUtils(), annotations, roundEnv);
    }
    return true;
  }

  private int processEntities(RoundEnvironment roundEnv) {
    int count = 0;
    for (Element element : roundEnv.getElementsAnnotatedWith(processingContext.embeddableAnnotation())) {
      generateQueryBeans(element);
      count++;
    }
    for (Element element : roundEnv.getElementsAnnotatedWith(processingContext.entityAnnotation())) {
      generateQueryBeans(element);
      count++;
    }
    for (Element element : roundEnv.getElementsAnnotatedWith(processingContext.mappedSuperclassAnnotation())) {
      addMappedSuperclasses(element);
    }
    return count;
  }

  private void addMappedSuperclasses(Element element) {
    String fullName = ((TypeElement)element).getQualifiedName().toString();
    processingContext.addMappedSuper(fullName);
  }

  private void processOthers(RoundEnvironment round) {
    processOthers(round, processingContext.converterAnnotation());
    processOthers(round, processingContext.componentAnnotation());
  }

  private void processOthers(RoundEnvironment roundEnv, TypeElement otherType) {
    if (otherType != null) {
      for (Element element : roundEnv.getElementsAnnotatedWith(otherType)) {
        processingContext.addOther(element);
      }
    }
  }

  /**
   * Write the {@code EbeanEntityRegister} at the end of processing - deferred until there is
   * something to actually register (rather than eagerly reserving/creating the source file up
   * front), since a compilation unit with no {@code @Entity}/{@code @Embeddable}/{@code @Converter}
   * /other classes at all - e.g. a test-source-only module that only declares
   * {@code @DtoMapping} - has nothing meaningful to write and no factory package to derive a
   * sensible location from.
   */
  private void writeModuleInfoBean() {
    if (!processingContext.hasAnyEntitiesOrOther()) {
      processingContext.logNote("EbeanEntityRegister skipped - no entities or other classes found");
      return;
    }
    try {
      SimpleModuleInfoWriter moduleWriter = new SimpleModuleInfoWriter(processingContext);
      moduleWriter.write();
    } catch (FilerException e) {
      processingContext.logWarn(null, "FilerException trying to write EntityClassRegister error: " + e);
    } catch (Throwable e) {
      processingContext.logError(null, "Failed to write EntityClassRegister error:" + e + " stack:" + Arrays.toString(e.getStackTrace()));
    }
  }

  /**
   * Generate the dto mappers as soon as all {@code @DtoMapping} pairs collected so far are
   * available - i.e. as early as possible, not deferred to {@code roundEnv.processingOver()}.
   * Generated types (unlike the {@code EbeanEntityRegister}/module-info registrations) are
   * directly referenced by hand-written source code, so they must be written in a round prior
   * to the final one or javac will not compile them at all (only a warning, no error, is
   * produced for files created in the very last round - see "will not be subject to annotation
   * processing").
   */
  private void writeDtoMappers() {
    if (wroteDtoMappers) {
      return;
    }
    try {
      var metas = dtoMappingReader.resolveAndValidate();
      if (metas.isEmpty()) {
        return;
      }
      wroteDtoMappers = true;
      for (DtoBeanMeta meta : metas) {
        new DtoMapperWriter(processingContext, meta).write();
      }
      new DtoMapperRegisterWriter(processingContext, metas).write();
      processingContext.logNote("Ebean APT generated %s dto mappers", metas.size());
    } catch (Throwable e) {
      wroteDtoMappers = true;
      processingContext.logError(null, "Failed to generate dto mappers error:" + e + " stack:" + Arrays.toString(e.getStackTrace()));
    }
  }

  private void generateQueryBeans(Element element) {
    try {
      SimpleQueryBeanWriter beanWriter = new SimpleQueryBeanWriter((TypeElement) element, processingContext);
      beanWriter.writeRootBean();
    } catch (Throwable e) {
      processingContext.logError(element, "Error generating query beans: " + e);
    }
  }

}
