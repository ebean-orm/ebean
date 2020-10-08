package io.ebean.querybean.generator;

import java.util.List;

class ModuleMeta {
  private final List<String> entities;
  private final List<String> other;

  ModuleMeta(List<String> entities, List<String> other) {
    this.entities = entities;
    this.other = other;
  }

  List<String> getEntities() {
    return entities;
  }

  List<String> getOther() {
    return other;
  }
}
