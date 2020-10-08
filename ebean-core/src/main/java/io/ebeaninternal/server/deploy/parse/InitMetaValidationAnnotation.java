package io.ebeaninternal.server.deploy.parse;

import javax.validation.constraints.Size;

class InitMetaValidationAnnotation {

  static void init(ReadAnnotationConfig readConfig) {
    readConfig.addMetaAnnotation(Size.class);
    readConfig.addMetaAnnotation(Size.List.class);
  }
}
