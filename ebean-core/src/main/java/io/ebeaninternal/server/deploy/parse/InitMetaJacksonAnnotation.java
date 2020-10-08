package io.ebeaninternal.server.deploy.parse;

class InitMetaJacksonAnnotation {

  static void init(ReadAnnotationConfig readConfig) {
    readConfig.addMetaAnnotation(com.fasterxml.jackson.annotation.JacksonAnnotation.class);
  }
}
