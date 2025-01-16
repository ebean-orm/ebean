package io.ebean.querybean.generator;

import static io.ebean.querybean.generator.APContext.logError;
import static io.ebean.querybean.generator.APContext.logNote;
import static io.ebean.querybean.generator.APContext.logWarn;
import static io.ebean.querybean.generator.APContext.typeElement;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.avaje.prism.GenerateAPContext;
import io.avaje.prism.GenerateModuleInfoReader;
import io.avaje.prism.GenerateUtils;

/** Process compiled entity beans and generates 'query beans' for them. */
@GenerateUtils
@GenerateAPContext
@GenerateModuleInfoReader
@SupportedAnnotationTypes({
  ConverterPrism.PRISM_TYPE,
  EbeanComponentPrism.PRISM_TYPE,
  EntityPrism.PRISM_TYPE,
  EmbeddablePrism.PRISM_TYPE,
  ModuleInfoPrism.PRISM_TYPE
})
public class Processor extends AbstractProcessor implements Constants {

  private SimpleModuleInfoWriter moduleWriter;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    ProcessingContext.init(processingEnv);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    APContext.setProjectModuleElement(annotations, roundEnv);
    ProcessingContext.readModuleInfo();
    int count = processEntities(roundEnv);
    processOthers(roundEnv);
    final int loaded = ProcessingContext.complete();
    initModuleInfoBean();
    if (count > 0) {
      String msg =
          "Ebean APT generated %s query beans, loaded %s others - META-INF/ebean-generated-info.mf entity-packages: %s";
      logNote(msg, count, loaded, ProcessingContext.getAllEntityPackages());
    }
    if (roundEnv.processingOver()) {
      writeModuleInfoBean();
      ProcessingContext.validateModule();
      ProcessingContext.clear();
    }
    return true;
  }

  private int processEntities(RoundEnvironment roundEnv) {
    int count = 0;

    for (Element element : getElements(roundEnv, EmbeddablePrism.PRISM_TYPE)) {
      generateQueryBeans(element);
      count++;
    }
    for (Element element : getElements(roundEnv, EntityPrism.PRISM_TYPE)) {
      generateQueryBeans(element);
      count++;
    }
    return count;
  }

  private void processOthers(RoundEnvironment round) {
    getElements(round, ConverterPrism.PRISM_TYPE).forEach(ProcessingContext::addOther);
    getElements(round, EbeanComponentPrism.PRISM_TYPE).forEach(ProcessingContext::addOther);
  }

  private Set<? extends Element> getElements(RoundEnvironment round, String name) {
    return Optional.ofNullable(typeElement(name))
        .map(round::getElementsAnnotatedWith)
        .orElse(Set.of());
  }

  private void initModuleInfoBean() {
    try {
      if (moduleWriter == null) {
        moduleWriter = new SimpleModuleInfoWriter();
      }
    } catch (FilerException e) {
      logWarn(null, "FilerException trying to write EntityClassRegister: " + e);
    } catch (Throwable e) {
      logError("Failed to write EntityClassRegister error:" + e + " stack:" + Arrays.toString(e.getStackTrace()));
    }
  }

  private void writeModuleInfoBean() {
    try {
      if (moduleWriter == null) {
        logError("EntityClassRegister was not initialised and not written");
      } else {
        moduleWriter.write();
      }
    } catch (Exception e) {
      e.printStackTrace();
      logError(
          "Failed to write EntityClassRegister error:"
              + e
              + " stack:"
              + Arrays.toString(e.getStackTrace()));
    }
  }

  private void generateQueryBeans(Element element) {
    try {
      SimpleQueryBeanWriter beanWriter =
          new SimpleQueryBeanWriter((TypeElement) element);
      beanWriter.writeRootBean();
    } catch (Exception e) {
      logError(element, "Error generating query beans: " + e);
    }
  }
}
