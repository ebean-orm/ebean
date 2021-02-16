package io.ebeaninternal.server.deploy.parse;

import jakarta.validation.constraints.Size;

class InitMetaJakartaValidationAnnotation {

  static void init(ReadAnnotationConfig readConfig) {
    readConfig.addMetaAnnotation(Size.class);
    readConfig.addMetaAnnotation(Size.List.class);
  }
}
