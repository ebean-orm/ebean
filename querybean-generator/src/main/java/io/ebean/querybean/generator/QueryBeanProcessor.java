package io.ebean.querybean.generator;

import static io.ebean.querybean.generator.APContext.logError;
import static io.ebean.querybean.generator.APContext.logNote;
import static io.ebean.querybean.generator.APContext.typeElement;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
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
@SupportedOptions("buildPlugin")
@SupportedAnnotationTypes({
  ConverterPrism.PRISM_TYPE,
  EbeanComponentPrism.PRISM_TYPE,
  EntityPrism.PRISM_TYPE,
  EmbeddablePrism.PRISM_TYPE,
  ModuleInfoPrism.PRISM_TYPE
})
public class QueryBeanProcessor extends AbstractProcessor implements Constants {

  private SimpleModuleInfoWriter moduleWriter;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    ProcessingContext.init(processingEnv);
    try {

      // write a note in target so that other apts can know spi is running
      var file = APContext.getBuildResource("avaje-processors.txt");
      var addition = new StringBuilder();
      //if file exists, dedup and append current processor
      if (file.toFile().exists()) {
        var result =
          Stream.concat(Files.lines(file), Stream.of("ebean-querybean-generator"))
            .distinct()
            .collect(joining("\n"));
        addition.append(result);
      } else {
        addition.append("ebean-querybean-generator");
      }
      Files.writeString(file, addition.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
      PomPluginWriter.addPlugin2Pom();
    } catch (IOException e) {
      // not an issue worth failing over
    }
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
    } catch (Exception e) {
      e.printStackTrace();
      logError(
          "Failed to initialise EntityClassRegister error:"
              + e
              + " stack:"
              + Arrays.toString(e.getStackTrace()));
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
      e.printStackTrace();
      logError(element, "Error generating query beans: " + e);
    }
  }
}
